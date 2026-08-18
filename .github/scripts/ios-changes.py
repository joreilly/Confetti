#!/usr/bin/env python3
"""Decide whether a pull request can affect the iOS build.

The default is deliberately conservative: unknown files run iOS CI. Known
platform-only paths are ignored, while changes to the Gradle version catalog
are traced to catalog accessors used by the shared/iOS build inputs.
"""

from __future__ import annotations

import os
from pathlib import Path
import subprocess
import sys
import tomllib
from typing import Any


ALWAYS_IOS_PATHS = (
    ".github/scripts/ios-changes.py",
    ".github/workflows/ios.yml",
    "build.gradle.kts",
    "gradle.properties",
    "gradle/wrapper/",
    "gradlew",
    "gradlew.bat",
    "iosApp/",
    "settings.gradle.kts",
    "shared/",
)

NON_IOS_PATHS = (
    ".devcontainer/",
    ".github/",
    ".idea/",
    "androidApp/",
    "androidBenchmark/",
    "automotiveApp/",
    "backend/",
    "common/car/",
    "compose-desktop/",
    "compose-web/",
    "design/",
    "docs/",
    "fastlane/",
    "landing-page/",
    "proto/",
    "tmp-previews/",
    "wearApp/",
    "wearBenchmark/",
)

CATALOG_PATH = "gradle/libs.versions.toml"


def git(*args: str) -> bytes:
    return subprocess.check_output(("git", *args))


def matches(path: str, patterns: tuple[str, ...]) -> bool:
    return any(path == pattern or (pattern.endswith("/") and path.startswith(pattern)) for pattern in patterns)


def catalog_at(revision: str) -> dict[str, Any]:
    return tomllib.loads(git("show", f"{revision}:{CATALOG_PATH}").decode())


def changed_catalog_entries(base: dict[str, Any], head: dict[str, Any]) -> set[tuple[str, str]]:
    changed: set[tuple[str, str]] = set()
    for section in ("versions", "libraries", "bundles", "plugins"):
        before = base.get(section, {})
        after = head.get(section, {})
        for key in before.keys() | after.keys():
            if before.get(key) != after.get(key):
                changed.add((section, key))
    return changed


def version_ref(entry: Any) -> str | None:
    if not isinstance(entry, dict):
        return None
    reference = entry.get("version.ref")
    if reference is None and isinstance(entry.get("version"), dict):
        reference = entry["version"].get("ref")
    return reference if isinstance(reference, str) else None


def accessor(section: str, alias: str) -> str:
    normalized = alias.replace("-", ".").replace("_", ".")
    prefix = {"libraries": "libs.", "bundles": "libs.bundles.", "plugins": "libs.plugins."}[section]
    return prefix + normalized


def relevant_catalog_accessors(base: dict[str, Any], head: dict[str, Any]) -> set[str]:
    changed = changed_catalog_entries(base, head)
    accessors: set[str] = set()
    affected_libraries: set[str] = set()

    for section, key in changed:
        if section != "versions":
            if section == "libraries":
                affected_libraries.add(key)
            else:
                accessors.add(accessor(section, key))
            continue

        # A named version affects every library/plugin that refers to it. Check
        # both revisions so renames and removals remain detectable.
        for catalog in (base, head):
            for dependent_section in ("libraries", "plugins"):
                for alias, entry in catalog.get(dependent_section, {}).items():
                    if version_ref(entry) == key:
                        if dependent_section == "libraries":
                            affected_libraries.add(alias)
                        else:
                            accessors.add(accessor(dependent_section, alias))

    accessors.update(accessor("libraries", alias) for alias in affected_libraries)
    # A shared source set may consume a changed library through a bundle rather
    # than through the library accessor itself.
    for catalog in (base, head):
        for alias, entries in catalog.get("bundles", {}).items():
            if isinstance(entries, list) and affected_libraries.intersection(entries):
                accessors.add(accessor("bundles", alias))

    return accessors


def ios_build_text() -> str:
    inputs = [Path("build.gradle.kts"), Path("settings.gradle.kts"), Path("shared/build.gradle.kts")]
    inputs.extend(Path("build-logic").rglob("*.gradle.kts"))
    inputs.extend(Path("build-logic").rglob("*.kt"))
    return "\n".join(path.read_text(errors="replace") for path in inputs if path.is_file())


def catalog_affects_ios(base_revision: str, head_revision: str) -> tuple[bool, str]:
    try:
        accessors = relevant_catalog_accessors(catalog_at(base_revision), catalog_at(head_revision))
    except (subprocess.CalledProcessError, tomllib.TOMLDecodeError, UnicodeDecodeError) as error:
        return True, f"could not classify the version catalog safely ({error})"

    used = sorted(item for item in accessors if item in ios_build_text())
    if used:
        return True, f"version catalog changes affect iOS build inputs: {', '.join(used)}"
    return False, "version catalog changes are not referenced by iOS build inputs"


def classify(base_revision: str, head_revision: str) -> tuple[bool, str]:
    # Use the merge base so updates merged into the target branch after a PR was
    # opened are not mistaken for changes made by the PR.
    merge_base = git("merge-base", base_revision, head_revision).decode().strip()
    changed = [item.decode() for item in git("diff", "--name-only", "-z", merge_base, head_revision).split(b"\0") if item]
    if not changed:
        return False, "no changed files"

    for path in changed:
        if matches(path, ALWAYS_IOS_PATHS):
            return True, f"{path} can affect the iOS build"

    catalog_changed = CATALOG_PATH in changed
    if catalog_changed:
        affects_ios, reason = catalog_affects_ios(merge_base, head_revision)
        if affects_ios:
            return True, reason

    remaining = [path for path in changed if path != CATALOG_PATH]
    unknown = [path for path in remaining if not matches(path, NON_IOS_PATHS) and not path.endswith((".md", ".txt"))]
    if unknown:
        return True, f"unclassified change is treated as iOS-relevant: {unknown[0]}"

    if catalog_changed:
        return False, "only non-iOS files changed; version catalog entries are not used by iOS"
    return False, "only known non-iOS files changed"


def main() -> int:
    if len(sys.argv) != 3:
        print(f"usage: {Path(sys.argv[0]).name} BASE_REVISION HEAD_REVISION", file=sys.stderr)
        return 2

    try:
        affects_ios, reason = classify(sys.argv[1], sys.argv[2])
    except subprocess.CalledProcessError as error:
        # A missing revision or unexpected git failure must never silently skip CI.
        affects_ios, reason = True, f"could not inspect changes safely ({error})"

    value = str(affects_ios).lower()
    print(f"ios={value}")
    print(f"iOS CI decision: {value} ({reason})")
    if output_file := os.environ.get("GITHUB_OUTPUT"):
        with open(output_file, "a", encoding="utf-8") as output:
            output.write(f"ios={value}\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
