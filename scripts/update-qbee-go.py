#!/usr/bin/env python3
"""Bump the qbee-go-native recipe to the latest stable Go release of a series.

Usage:
    scripts/update-qbee-go.py            # latest release of the series currently in the recipe
    scripts/update-qbee-go.py 1.28       # move to (or update within) another series
    scripts/update-qbee-go.py --dry-run

The release list and sha256 checksums come from https://go.dev/dl/?mode=json.
The recipe file is renamed to the new version (with `git mv` when tracked) and
every SRC_URI[go_linux_<arch>.sha256sum] entry in it is updated.

Prints the new version on stdout when the recipe changed, and nothing when it
is already up to date, so CI can use the output to decide whether to open a PR.
"""

import argparse
import json
import pathlib
import re
import subprocess
import sys
import urllib.request

DL_URL = "https://go.dev/dl/?mode=json&include=all"
REPO_ROOT = pathlib.Path(__file__).resolve().parent.parent
RECIPE_DIR = REPO_ROOT / "meta-qbee" / "recipes-devtools" / "qbee-go"
RECIPE_GLOB = "qbee-go-native_*.bb"
SUM_RE = re.compile(r'^(SRC_URI\[go_linux_(?P<arch>[a-z0-9]+)\.sha256sum\]\s*=\s*)"[0-9a-f]*"', re.M)


def parse_version(v):
    return tuple(int(x) for x in v.split("."))


def find_recipe():
    recipes = sorted(RECIPE_DIR.glob(RECIPE_GLOB))
    if len(recipes) != 1:
        sys.exit(f"expected exactly one {RECIPE_GLOB} in {RECIPE_DIR}, found {len(recipes)}")
    return recipes[0]


def latest_release(series):
    with urllib.request.urlopen(DL_URL, timeout=60) as resp:
        releases = json.load(resp)
    candidates = []
    for rel in releases:
        ver = rel["version"].removeprefix("go")
        if not rel.get("stable") or not re.fullmatch(r"\d+\.\d+(\.\d+)?", ver):
            continue
        if ver == series or ver.startswith(series + "."):
            candidates.append((parse_version(ver), ver, rel))
    if not candidates:
        sys.exit(f"no stable Go release found for series {series}")
    _, ver, rel = max(candidates)
    return ver, rel


def is_tracked(path):
    return subprocess.run(
        ["git", "ls-files", "--error-unmatch", str(path)],
        cwd=REPO_ROOT, capture_output=True,
    ).returncode == 0


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("series", nargs="?", help="Go release series, e.g. 1.27 (default: series of current recipe)")
    ap.add_argument("--dry-run", action="store_true", help="show what would change without writing")
    args = ap.parse_args()

    old = find_recipe()
    cur_ver = old.stem.split("_", 1)[1]
    series = args.series or ".".join(cur_ver.split(".")[:2])

    ver, rel = latest_release(series)
    new = old.with_name(f"qbee-go-native_{ver}.bb")

    sums = {
        f["arch"]: f["sha256"]
        for f in rel["files"]
        if f["os"] == "linux" and f["kind"] == "archive"
    }

    text = old.read_text()
    arches = [m.group("arch") for m in SUM_RE.finditer(text)]
    if not arches:
        sys.exit(f"no SRC_URI[go_linux_<arch>.sha256sum] entries found in {old.name}")
    missing = [a for a in arches if a not in sums]
    if missing:
        sys.exit(f"go{ver} has no linux archive for: {', '.join(missing)}")

    new_text = SUM_RE.sub(lambda m: f'{m.group(1)}"{sums[m.group("arch")]}"', text)

    if new == old and new_text == text:
        print(f"qbee-go-native already at {ver}", file=sys.stderr)
        return

    print(f"qbee-go-native: {cur_ver} -> {ver}", file=sys.stderr)
    for a in arches:
        print(f"  linux/{a}: {sums[a]}", file=sys.stderr)
    if args.dry_run:
        return

    if new != old:
        if is_tracked(old):
            subprocess.run(["git", "mv", str(old), str(new)], cwd=REPO_ROOT, check=True)
        else:
            old.rename(new)
    new.write_text(new_text)
    print(ver)


if __name__ == "__main__":
    main()
