#!/usr/bin/env bash

# The website is built using MkDocs with the Material theme.
# https://squidfunk.github.io/mkdocs-material/
# It requires Python to run.
# Install the packages with the following command:
# Install the packages: `pip install mkdocs-material`
# Build the api docs: `./gradlew dokkaHtmlMultiModule`
# Then run this script to prepare the docs for the website.
# Finally, run `mkdocs serve` to preview the site locally or `mkdocs build` to build the site.

set -ex

# Copy the changelog into the site, omitting the unreleased section
cat CHANGELOG.md \
 | grep -v '^## Unreleased' \
 | sed '/^## /,$!d' \
 > docs/changelog.md


# Add the jinja frontmatter to the index
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
