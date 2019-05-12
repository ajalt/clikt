[clikt](../index.md) / [com.github.ajalt.clikt.parameters.groups](index.md) / [cooccurring](./cooccurring.md)

# cooccurring

`fun <T : `[`OptionGroup`](-option-group/index.md)`> `[`T`](cooccurring.md#T)`.cooccurring(): `[`CoOccurringOptionGroup`](-co-occurring-option-group/index.md)`<`[`T`](cooccurring.md#T)`, `[`T`](cooccurring.md#T)`?>`

Make this group a co-occurring group.

The group becomes nullable. At least one option in the group must be [required](required.md). Of none of the
options in the group are given on the command line, the group is null and none of the `required`
constraints are enforced. If any option in the group is given, all `required` options in the
group must be given as well.

