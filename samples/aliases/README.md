# Command aliases

This example shows how you can load command aliases dynamically from a
file. Aliases are read from the `aliases.cfg` file.

With the example alias, the following two calls will produce the same
result:

```
./runsample aliases commit -m "commit message"
./runsample aliases cm "commit message"
```
