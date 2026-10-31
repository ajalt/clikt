#!/usr/bin/env bash

set -euo pipefail

fail() { echo "$1" >&2; exit 1; }
replace() { sed -E "$1" "$2" > "$2.tmp" && mv "$2.tmp" "$2"; }
confirm() { read -rp "$1 [y/N] " answer && [[ $answer == [yY] ]]; }

(( $# <= 1 )) || fail "usage: $0 [version]"
cd "$(dirname "$0")"
current=$(sed -n 's/^VERSION_NAME=//p' gradle.properties)

if (( $# == 1 )); then
  version=$1
else
  echo "Current version: $current"
  read -rp "New version: " version || fail "Aborted"
fi
[[ $version =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || fail "Invalid version '$version', expected a version like 3.8.0"

[[ $(git branch --show-current) == master ]] || fail "Not on master"
[[ -z $(git status --porcelain) ]] || fail "Working tree has uncommitted changes"
git rev-parse -q --verify "refs/tags/$version" > /dev/null && fail "Tag $version already exists"

notes=$(awk '/^## Unreleased$/ {flag=1; next} /^## /{flag=0} flag' CHANGELOG.md)
[[ -n ${notes//[[:space:]]/} ]] || fail "CHANGELOG.md has no entries under '## Unreleased'"

group=$(sed -n 's/^GROUP=//p' gradle.properties)

printf 'Releasing %s (currently %s) with these changes:\n\n%s\n\n' "$version" "$current" "$notes"
confirm "Commit $version?" || fail "Aborted"

replace "s/^VERSION_NAME=.*/VERSION_NAME=$version/" gradle.properties
replace "s/^## Unreleased$/## $version/" CHANGELOG.md
replace "s/(\"${group//./\\.}:[^:\"]+:)[^\"]+\"/\1$version\"/g" README.md
git commit -am "Release version $version"

if ! confirm "Push master and tag $version?"; then
  printf '\nTo publish, run:\n  git push origin master\n  git tag %s\n  git push origin %s\n' "$version" "$version"
  exit 0
fi
git push origin master
git tag "$version"
git push origin "$version"
