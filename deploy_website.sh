#!/usr/bin/env bash

set -ex

./gradlew dokka
mkdocs gh-deploy
