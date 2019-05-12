[clikt](../index.md) / [com.github.ajalt.clikt.parameters.arguments](index.md) / [validate](./validate.md)

# validate

`fun <AllT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, ValueT> `[`ProcessedArgument`](-processed-argument/index.md)`<`[`AllT`](validate.md#AllT)`, `[`ValueT`](validate.md#ValueT)`>.validate(validator: `[`ArgValidator`](-arg-validator.md)`<`[`AllT`](validate.md#AllT)`>): `[`ArgumentDelegate`](-argument-delegate/index.md)`<`[`AllT`](validate.md#AllT)`>`

Check the final argument value and raise an error if it's not valid.

The [validator](validate.md#com.github.ajalt.clikt.parameters.arguments$validate(com.github.ajalt.clikt.parameters.arguments.ProcessedArgument((com.github.ajalt.clikt.parameters.arguments.validate.AllT, com.github.ajalt.clikt.parameters.arguments.validate.ValueT)), kotlin.Function2((com.github.ajalt.clikt.parameters.arguments.ArgumentTransformContext, com.github.ajalt.clikt.parameters.arguments.validate.AllT, kotlin.Unit)))/validator) is called with the final argument type (the output of [transformAll](../com.github.ajalt.clikt.parameters.options/transform-all.md)), and should call
`fail` if the value is not valid.

You can also call `require` to fail automatically if an expression is false.

### Example:

``` kotlin
val opt by argument().int().validate { require(it % 2 == 0) { "value must be even" } }
```

