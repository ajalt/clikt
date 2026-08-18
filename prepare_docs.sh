#!/usr/bin/env bash

# The website is built using Zensical, configured by zensical.toml.
# https://zensical.org/
# Zensical requires Python to run.
# Install the packages: `pip install zensical`
# Build the api docs: `./gradlew dokkaGenerateHtml`
# Then run this script to prepare the docs for the website.
# Finally, run `zensical serve` to preview the site locally or `zensical build` to build the site.

set -ex

# Copy the changelog into the site, omitting the unreleased section
cat CHANGELOG.md \
 | grep -v '^## Unreleased' \
 | sed '/^## /,$!d' \
 > docs/changelog.md


# Add the frontmatter to the index
cat > docs/index.md <<- EOM
---
hide:
  - toc        # Hide table of contents
---

EOM

# Copy the README into the index, omitting the license, docs links, and fixing hrefs
cat README.md \
  | sed 's:docs/img:img:g' \
  | sed -e '/## Documentation/,/(runsample)\./d' \
  | sed '/## License/Q' \
  >> docs/index.md

# Add some extra links to the index page
cat >> docs/index.md <<- EOM

# API Reference

* [Commands and Exceptions](api/clikt/com.github.ajalt.clikt.core/)
* [Options](api/clikt/com.github.ajalt.clikt.parameters.options/)
* [Arguments](api/clikt/com.github.ajalt.clikt.parameters.arguments/)
* [Parameter Type Conversions](api/clikt/com.github.ajalt.clikt.parameters.types/)
* [Output Formatting](api/clikt/com.github.ajalt.clikt.output/)
EOM
