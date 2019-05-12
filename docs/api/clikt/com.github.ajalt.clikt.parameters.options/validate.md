[clikt](../index.md) / [com.github.ajalt.clikt.parameters.options](index.md) / [validate](./validate.md)

# validate

`fun <T : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`> `[`FlagOption`](-flag-option/index.md)`<`[`T`](validate.md#T)`>.validate(validator: `[`OptionValidator`](-option-validator.md)`<`[`T`](validate.md#T)`>): `[`OptionDelegate`](-option-delegate/index.md)`<`[`T`](validate.md#T)`>`

Check the final option value and raise an error if it's not valid.

The `validator` is called with the final option type (the output of [transformAll](transform-all.md)), and should call `fail`
if the value is not valid. It is not called if the delegate value is null.

`fun <AllT : `[`Any`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)`, EachT, ValueT> `[`OptionWithValues`](-option-with-values/index.md)`<`[`AllT`](validate.md#AllT)`, `[`EachT`](validate.md#EachT)`, `[`ValueT`](validate.md#ValueT)`>.validate(validator: `[`OptionValidator`](-option-validator.md)`<`[`AllT`](validate.md#AllT)`>): `[`OptionDelegate`](-option-delegate/index.md)`<`[`AllT`](validate.md#AllT)`>`

Check the final option value and raise an error if it's not valid.

The `validator` is called with the final option type (the output of [transformAll](transform-all.md)), and should call `fail`
if the value is not valid. It is not called if the delegate value is null.

You can also call `require` to fail automatically if an expression is false.

### Example:

``` kotlin
val opt by option().int().validate { require(it % 2 == 0) { "value must be even" } }
```

