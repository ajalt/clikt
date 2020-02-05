#!/usr/bin/env bash

set -ex

./gradlew dokkaPostProcess
mkdocs gh-deploy
