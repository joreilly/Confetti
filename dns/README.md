# DNS

DNS-as-code for `confetti-app.dev`, hosted at [Porkbun](https://porkbun.com). Records
live in [`zones/confetti-app.dev.yaml`](zones/confetti-app.dev.yaml) and are kept in
sync with Porkbun by [octoDNS](https://github.com/octodns/octodns) via the
[octodns-porkbun](https://github.com/major/octodns-porkbun) provider.

Workflow:

1. Edit `zones/confetti-app.dev.yaml` on a branch and open a PR.
2. GitHub Actions (`.github/workflows/dns-plan.yml`) runs `octodns-sync` in
   dry-run mode and posts the diff as a PR comment.
3. Review the diff, get it approved, merge.
4. GitHub Actions (`.github/workflows/dns-apply.yml`) runs `octodns-sync
   --doit` on `main`, which pushes the change to Porkbun.

There is no Terraform-style state file: octoDNS diffs the YAML in this folder
directly against Porkbun's live records on every run. Records that exist at
Porkbun but not in the YAML are **deleted** on apply.

## One-time setup

1. **Enable API access for the domain.** In the Porkbun dashboard, under
   Domain Management for `confetti-app.dev`, toggle "API Access" on. This is
   separate from having an account-level API key.
2. **Create an API key.** [porkbun.com/account/api](https://porkbun.com/account/api)
   → generate a key. You get an API Key (`pk1_...`) and a Secret API Key
   (`sk1_...`).
3. **Add repo secrets.** In the repo's Settings → Secrets and variables →
   Actions, add:
   - `PORKBUN_API_KEY`
   - `PORKBUN_SECRET_KEY`
4. **(Recommended) Protect the `dns` environment.** The apply workflow runs
   under the `dns` GitHub environment (Settings → Environments → New
   environment → `dns`). Add required reviewers there if you want a manual
   approval gate on top of PR review before changes actually hit Porkbun.
5. **Seed real records** (see below) before merging anything for real. The
   committed `zones/confetti-app.dev.yaml` is a placeholder built from public
   DNS lookups and may be missing records.

## Seeding real records

To replace the placeholder with what's actually live at Porkbun:

```bash
cd dns
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt

export PORKBUN_API_KEY=pk1_...
export PORKBUN_SECRET_KEY=sk1_...

octodns-dump --config-file=config/octodns.yaml --output-dir=zones --lenient confetti-app.dev. porkbun
```

This overwrites `zones/confetti-app.dev.yaml` with the current live records.
Review the diff, remove the placeholder comment, and commit.

## Local dry run

```bash
cd dns
source .venv/bin/activate
export PORKBUN_API_KEY=pk1_...
export PORKBUN_SECRET_KEY=sk1_...

octodns-validate --config-file=config/octodns.yaml     # syntax/config check only
octodns-sync --config-file=config/octodns.yaml         # dry run, prints the plan
octodns-sync --config-file=config/octodns.yaml --doit  # actually apply
```

## Layout

```
config/octodns.yaml         # octoDNS provider config (source: ./zones, target: Porkbun)
zones/confetti-app.dev.yaml # source of truth for confetti-app.dev records
requirements.txt            # Python dependencies
```

The workflows live in the repository's `.github/workflows/dns-*.yml`.
