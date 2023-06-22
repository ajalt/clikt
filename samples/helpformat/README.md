# Custom help formatter

This example shows how to use a custom help formatter in a command. This
formatter changes the parameter sections to be drawn in panels, and changes the colors used.

```
$ ./runsample helpformat --help
Usage: echo [OPTIONS] [STRINGS]...   
                                                                  
  Echo the STRING(s) to standard output

╭─ Options: ────────────────────────────────────────╮
│  -n          do not output the trailing newline   │
│  -h, --help  show this message and exit           │
╰───────────────────────────────────────────────────╯
╭─ Arguments: ──────────────────────────────────────╮
│  STRINGS  the strings to echo                     │
╰───────────────────────────────────────────────────╯
```
