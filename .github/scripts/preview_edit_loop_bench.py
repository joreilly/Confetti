#!/usr/bin/env python3
"""Edit -> render benchmark for Compose @Previews, via compose-preview MCP and CLI.

Measures how long an agent waits between saving a source edit and getting new pixels back, for
one Android preview (Robolectric) and one Compose Multiplatform preview (desktop/Skia). Each case
edits a visible string, renders, and checks the image actually changed.

  mcp  Drives `compose-preview mcp serve` over stdio, as an agent host would: one warm-up render
       (not counted), then N edit cycles. The server's per-cycle work record (compile path,
       compile ms, render ms) is read from its stderr so a regression can be attributed.
  cli  `compose-preview show --json --filter <preview>` per cycle: a full Gradle invocation each
       time. Fewer cycles, since each costs tens of seconds.

Writes a JSON report and a Markdown table (appended to $GITHUB_STEP_SUMMARY when set). Exits 1
when a case's median warm edit exceeds its budget, or when an edit did not change the image.
Source files are restored afterwards whatever happens.
"""

import argparse
import json
import os
import queue
import statistics
import subprocess
import sys
import threading
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

# `budget_ms` gates the median warm MCP edit (cycle 1 is excluded: it can carry first-compile
# costs). Generous on purpose, so CI catches regressions rather than runner noise; tighten as the
# loop gets faster. `target_ms` is the goal, reported but not enforced.
CASES = [
    {
        "name": "android",
        "preview": "AndroidSessionDetailPreview",
        "file": "androidApp/src/main/java/dev/johnoreilly/confetti/ui/AndroidScreenPreviews.kt",
        "original": "            session = sessionDetails,\n",
        "edited": '            session = sessionDetails.copy(title = "Edit loop {n}"),\n',
        "budget_ms": int(os.environ.get("EDIT_LOOP_BUDGET_ANDROID_MS", "10000")),
        "target_ms": 2000,
    },
    {
        "name": "cmp",
        "preview": "EmptyViewPreview",
        "file": "shared/src/commonMain/kotlin/dev/johnoreilly/confetti/ui/component/EmptyView.kt",
        "original": 'text = "No bookmarks yet"',
        "edited": 'text = "Edit loop {n}"',
        "budget_ms": int(os.environ.get("EDIT_LOOP_BUDGET_CMP_MS", "45000")),
        "target_ms": 2000,
    },
]


# Unique per run: a repeated edit string would let Gradle's build cache (local or remote) serve the
# compiled classes, and the benchmark would time a cache hit instead of a compile.
RUN_NONCE = f"{int(time.time()):x}"


class SourceEditor:
    """Rewrites one occurrence of a case's anchor; restores the original file on exit."""

    def __init__(self, case):
        self.path = ROOT / case["file"]
        self.original_text = self.path.read_text()
        if self.original_text.count(case["original"]) != 1:
            raise SystemExit(f"{case['file']}: expected exactly one {case['original']!r}")
        self.case = case

    def edit(self, n):
        self.path.write_text(
            self.original_text.replace(self.case["original"], self.case["edited"].format(n=f"{RUN_NONCE}-{n}"))
        )

    def restore(self):
        self.path.write_text(self.original_text)


class McpClient:
    def __init__(self, stderr_path):
        self.stderr_path = stderr_path
        self.proc = subprocess.Popen(
            ["compose-preview", "mcp", "serve"],
            cwd=ROOT,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=open(stderr_path, "w"),
            text=True,
            bufsize=1,
        )
        self.messages = queue.Queue()
        self.next_id = 0
        threading.Thread(target=self._read, daemon=True).start()

    def _read(self):
        for line in self.proc.stdout:
            line = line.strip()
            if line:
                try:
                    self.messages.put(json.loads(line))
                except ValueError:
                    pass
        self.messages.put(None)  # server exited

    def _send(self, message):
        self.proc.stdin.write(json.dumps({"jsonrpc": "2.0", **message}) + "\n")
        self.proc.stdin.flush()

    def request(self, method, params, timeout=1200):
        self.next_id += 1
        request_id = self.next_id
        self._send({"id": request_id, "method": method, "params": params})
        deadline = time.time() + timeout
        while True:
            message = self.messages.get(timeout=max(1, deadline - time.time()))
            if message is None:
                raise SystemExit(f"MCP server exited; see {self.stderr_path}")
            if message.get("id") == request_id and ("result" in message or "error" in message):
                if "error" in message:
                    raise SystemExit(f"{method} failed: {message['error']}")
                return message["result"]
            if "method" in message and "id" in message:  # a server-to-client request
                result = {}
                if message["method"] == "roots/list":
                    result = {"roots": [{"uri": ROOT.as_uri(), "name": ROOT.name}]}
                self._send({"id": message["id"], "result": result})

    def initialize(self):
        self.request(
            "initialize",
            {
                "protocolVersion": "2025-06-18",
                "capabilities": {"roots": {}},
                "clientInfo": {"name": "edit-loop-bench", "version": "1"},
            },
        )
        self._send({"method": "notifications/initialized", "params": {}})

    def render(self, preview):
        """render_preview until it stops answering `pending`; returns (structured, wall ms)."""
        started = time.monotonic()
        while True:
            result = self.request(
                "tools/call",
                {"name": "render_preview", "arguments": {"preview": preview, "inline": False}},
            )
            structured = result.get("structuredContent") or {}
            if result.get("isError"):
                text = " ".join(c.get("text", "") for c in result.get("content", []))
                raise SystemExit(f"render_preview {preview} failed: {text[:2000]}")
            if not structured.get("pending"):
                return structured, round((time.monotonic() - started) * 1000)
            time.sleep(structured.get("retryAfterMs", 1000) / 1000)

    def work_records(self):
        """The server's per-cycle work records, in order: compile path/ms and render ms."""
        records = []
        prefix = "compose-preview-mcp: edit cycle "
        for line in Path(self.stderr_path).read_text(errors="replace").splitlines():
            if line.startswith(prefix) and "{" in line:
                try:
                    records.append(json.loads(line[line.index("{"):]))
                except ValueError:
                    pass
        return records

    def close(self):
        try:
            self.proc.stdin.close()
            self.proc.wait(timeout=60)
        except Exception:
            self.proc.kill()


def summarize_work(record):
    if not record:
        return {}
    compile_work = record.get("compile") or {}
    executed = [
        t
        for t in compile_work.get("tasks", [])
        if not t.endswith((" UP-TO-DATE", " NO-SOURCE", " FROM-CACHE", " SKIPPED"))
    ]
    return {
        "compileTask": compile_work.get("task"),
        "compileMs": compile_work.get("ms"),
        "renderMs": record.get("renderMs"),
        "executedTasks": executed,
        "cachedTasks": [t for t in compile_work.get("tasks", []) if t.endswith(" FROM-CACHE")],
    }


def run_mcp(cases, cycles, out_dir):
    client = McpClient(out_dir / "mcp-server.log")
    results = []
    try:
        client.initialize()
        for case in cases:
            editor = SourceEditor(case)
            try:
                _, warm_ms = client.render(case["preview"])
                previous = None
                case_cycles = []
                for n in range(1, cycles + 1):
                    seen = len(client.work_records())
                    editor.edit(n)
                    structured, wall_ms = client.render(case["preview"])
                    new_records = client.work_records()[seen:]
                    sha = structured.get("sha256")
                    case_cycles.append(
                        {
                            "cycle": n,
                            "wallMs": wall_ms,
                            "sha256": sha,
                            "imageChanged": sha is not None and sha != previous,
                            **summarize_work(new_records[-1] if new_records else None),
                        }
                    )
                    previous = sha
                    print(f"[mcp] {case['name']} edit {n}: {wall_ms} ms", flush=True)
            finally:
                editor.restore()
            results.append({"mode": "mcp", "case": case, "warmUpMs": warm_ms, "cycles": case_cycles})
    finally:
        client.close()
    return results


def run_cli(cases, cycles, out_dir):
    results = []
    for case in cases:
        editor = SourceEditor(case)
        case_cycles = []
        try:
            for n in range(0, cycles + 1):  # n == 0 renders the unedited source as a warm-up
                if n:
                    editor.edit(n)
                started = time.monotonic()
                proc = subprocess.run(
                    ["compose-preview", "show", "--json", "--filter", case["preview"]],
                    cwd=ROOT,
                    capture_output=True,
                    text=True,
                )
                wall_ms = round((time.monotonic() - started) * 1000)
                (out_dir / f"cli-{case['name']}-{n}.log").write_text(proc.stderr)
                if proc.returncode != 0:
                    raise SystemExit(f"compose-preview show {case['preview']} exited {proc.returncode}")
                rows = json.loads(proc.stdout).get("previews", [])
                if n == 0:
                    warm_ms = wall_ms
                else:
                    case_cycles.append(
                        {
                            "cycle": n,
                            "wallMs": wall_ms,
                            "imageChanged": bool(rows) and all(r.get("changed") for r in rows),
                        }
                    )
                    print(f"[cli] {case['name']} edit {n}: {wall_ms} ms", flush=True)
        finally:
            editor.restore()
        results.append({"mode": "cli", "case": case, "warmUpMs": warm_ms, "cycles": case_cycles})
    return results


def warm_median(cycles):
    warm = [c["wallMs"] for c in cycles[1:]] or [c["wallMs"] for c in cycles]
    return round(statistics.median(warm))


def report(results, out_dir, errors=()):
    failures = list(errors)
    lines = [
        "## Compose preview edit → render",
        "",
        "| Mode | Case | Warm-up | Edits (ms) | Warm median | Budget | Target | Compile path (last) |",
        "|---|---|---|---|---|---|---|---|",
    ]
    for r in results:
        case, cycles = r["case"], r["cycles"]
        if not cycles:
            continue
        median = warm_median(cycles)
        gated = r["mode"] == "mcp"
        last = cycles[-1] if cycles else {}
        compile_path = (
            f"`{last.get('compileTask')}` {last.get('compileMs')} ms + render {last.get('renderMs')} ms"
            if last.get("compileTask")
            else "—"
        )
        lines.append(
            f"| {r['mode']} | {case['name']} (`{case['preview']}`) | {r['warmUpMs']} | "
            f"{', '.join(str(c['wallMs']) for c in cycles)} | **{median}** | "
            f"{case['budget_ms'] if gated else '—'} | {case['target_ms']} | {compile_path} |"
        )
        if gated and median > case["budget_ms"]:
            failures.append(f"{r['mode']}/{case['name']}: warm median {median} ms > budget {case['budget_ms']} ms")
        for c in cycles:
            if not c["imageChanged"]:
                failures.append(f"{r['mode']}/{case['name']}: edit {c['cycle']} did not change the image")
    if failures:
        lines += ["", "**Failures**", ""] + [f"- {f}" for f in failures]
    markdown = "\n".join(lines) + "\n"
    print(markdown)
    (out_dir / "summary.md").write_text(markdown)
    serializable = [{**r, "case": {k: v for k, v in r["case"].items() if k not in ("original", "edited")}} for r in results]
    (out_dir / "report.json").write_text(json.dumps(serializable, indent=2))
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a") as f:
            f.write(markdown)
    return failures


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--modes", default="mcp,cli", help="comma-separated: mcp, cli")
    parser.add_argument("--cases", default=",".join(c["name"] for c in CASES))
    parser.add_argument("--mcp-cycles", type=int, default=5)
    parser.add_argument("--cli-cycles", type=int, default=2)
    parser.add_argument("--out", default=str(ROOT / "build" / "edit-loop"))
    args = parser.parse_args()

    out_dir = Path(args.out)
    out_dir.mkdir(parents=True, exist_ok=True)
    wanted = set(args.cases.split(","))
    cases = [c for c in CASES if c["name"] in wanted]
    modes = args.modes.split(",")

    # CLI first: an MCP session leaves daemon output under build/compose-previews that a later
    # Gradle render has to hash, so running it second would measure (or trip over) that state.
    results, errors = [], []
    for mode, run, cycles in (("cli", run_cli, args.cli_cycles), ("mcp", run_mcp, args.mcp_cycles)):
        if mode not in modes:
            continue
        try:
            results += run(cases, cycles, out_dir)
        except SystemExit as e:
            errors.append(f"{mode}: {e}")
            print(f"[{mode}] aborted: {e}", file=sys.stderr, flush=True)
    failures = report(results, out_dir, errors)
    sys.exit(1 if failures else 0)


if __name__ == "__main__":
    main()
