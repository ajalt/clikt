# Validation example

This sample shows how to perform custom conversion and validation of
parameters.

* The `--count` option has a custom validator
* The `--bigger-count` option validates its value based on the value of `--count`
* The `--quad` option takes four integer values
* The `--sum` option can be provided multiple times, and all values will be added.
* The `URL` argument is converted to a java URL object, and must be in the form `http://www.example.com`


```
./runsample validation --count 2 --bigger-count 3 --quad 1 2 3 4 --sum 3 --sum 4 http://www.example.com
```
