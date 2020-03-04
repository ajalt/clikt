# Vendored Kotest copy

This directory contains copies of the parts the we use of
[kotest-assertions](https://github.com/kotest/kotest) at commit b6a198e. The code is slightly
modified to avoid needing and `actual`/`expect` declarations.

We copied the code to fix the following issues:

- No non-suspend versions of table testing in the common source set. This means that we can't call those functions.
- Native publishing broken: native targets are not being published
- No version published on Kotlin 1.3.70. You can only consume native dependencies that are compiled with the same compiler.

Hopefully these issues will be fixed and we can go back to using the normal dependencies.

# License

All code in this directory is subject to the following license:

```
Copyright 2016 sksamuel

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
