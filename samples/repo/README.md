# Repo example

This sample demonstrates building a complex commline app like git or hg.
It has multiple subcommands, and uses the `Context.obj` to send data
from the root command to subcommands.

```
./runsample repo --help
./runsample repo clone --help
```

Note that the gradle run task (and, by extension, `runsample`) doesn't
support interactive input, so if you want to call the `setuser`
subcommand, you should use the built distribution.
