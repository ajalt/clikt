

### All Types

| Name | Summary |
|---|---|
| [com.github.ajalt.clikt.core.Abort](../com.github.ajalt.clikt.core/-abort/index.md) | An internal error that signals Clikt to abort. |
| [com.github.ajalt.clikt.parameters.arguments.ArgCallsTransformer](../com.github.ajalt.clikt.parameters.arguments/-arg-calls-transformer.md) | A callback that transforms all the values into the final argument type |
| [com.github.ajalt.clikt.parameters.options.ArgsTransformer](../com.github.ajalt.clikt.parameters.options/-args-transformer.md) | A callback that transforms all the values for a call to the call type. |
| [com.github.ajalt.clikt.parameters.arguments.Argument](../com.github.ajalt.clikt.parameters.arguments/-argument/index.md) | A positional parameter to a command. |
| [com.github.ajalt.clikt.parameters.arguments.ArgumentDelegate](../com.github.ajalt.clikt.parameters.arguments/-argument-delegate/index.md) | An argument that functions as a property delegate |
| [com.github.ajalt.clikt.parameters.arguments.ArgumentTransformContext](../com.github.ajalt.clikt.parameters.arguments/-argument-transform-context/index.md) | A receiver for argument transformers. |
| [com.github.ajalt.clikt.parameters.arguments.ArgValidator](../com.github.ajalt.clikt.parameters.arguments/-arg-validator.md) | A callback validates the final argument type |
| [com.github.ajalt.clikt.parameters.arguments.ArgValueTransformer](../com.github.ajalt.clikt.parameters.arguments/-arg-value-transformer.md) | A callback that transforms a single value from a string to the value type |
| [com.github.ajalt.clikt.core.BadParameterValue](../com.github.ajalt.clikt.core/-bad-parameter-value/index.md) | A parameter was given the correct number of values, but of invalid format or type. |
| [com.github.ajalt.clikt.parameters.options.CallsTransformer](../com.github.ajalt.clikt.parameters.options/-calls-transformer.md) | A callback that transforms all of the calls to the final option type. |
| [com.github.ajalt.clikt.parameters.groups.ChoiceGroup](../com.github.ajalt.clikt.parameters.groups/-choice-group/index.md) |  |
| [com.github.ajalt.clikt.core.CliktCommand](../com.github.ajalt.clikt.core/-clikt-command/index.md) | The [CliktCommand](../com.github.ajalt.clikt.core/-clikt-command/index.md) is the core of command line interfaces in Clikt. |
| [com.github.ajalt.clikt.output.CliktConsole](../com.github.ajalt.clikt.output/-clikt-console/index.md) | An object that is used by commands and parameters to show text to the user and read input. |
| [com.github.ajalt.clikt.core.CliktError](../com.github.ajalt.clikt.core/-clikt-error/index.md) | An exception during command line processing that should be shown to the user. |
| [com.github.ajalt.clikt.output.CliktHelpFormatter](../com.github.ajalt.clikt.output/-clikt-help-formatter/index.md) |  |
| [com.github.ajalt.clikt.completion.CompletionCandidates](../com.github.ajalt.clikt.completion/-completion-candidates/index.md) | Configurations for generating shell autocomplete suggestions |
| [com.github.ajalt.clikt.core.Context](../com.github.ajalt.clikt.core/-context/index.md) | A object used to control command line parsing and pass data between commands. |
| [com.github.ajalt.clikt.parameters.groups.CoOccurringOptionGroup](../com.github.ajalt.clikt.parameters.groups/-co-occurring-option-group/index.md) |  |
| [com.github.ajalt.clikt.parameters.options.EagerOption](../com.github.ajalt.clikt.parameters.options/-eager-option/index.md) | An [Option](../com.github.ajalt.clikt.parameters.options/-option/index.md) with no values that is [finalize](../com.github.ajalt.clikt.parameters.options/-eager-option/finalize.md)d before other types of options. |
| [com.github.ajalt.clikt.parameters.options.FlagOption](../com.github.ajalt.clikt.parameters.options/-flag-option/index.md) | An [Option](../com.github.ajalt.clikt.parameters.options/-option/index.md) that has no values. |
| [com.github.ajalt.clikt.parsers.FlagOptionParser](../com.github.ajalt.clikt.parsers/-flag-option-parser/index.md) | A parser for options that take no values. |
| [com.github.ajalt.clikt.core.GroupableOption](../com.github.ajalt.clikt.core/-groupable-option/index.md) | An option that can be added to a [ParameterGroup](../com.github.ajalt.clikt.parameters.groups/-parameter-group/index.md) |
| [com.github.ajalt.clikt.output.HelpFormatter](../com.github.ajalt.clikt.output/-help-formatter/index.md) | Creates help and usage strings for a command. |
| [com.github.ajalt.clikt.core.IncorrectArgumentValueCount](../com.github.ajalt.clikt.core/-incorrect-argument-value-count/index.md) | An argument was supplied but the number of values supplied was incorrect. |
| [com.github.ajalt.clikt.core.IncorrectOptionValueCount](../com.github.ajalt.clikt.core/-incorrect-option-value-count/index.md) | An option was supplied but the number of values supplied to the option was incorrect. |
| [com.github.ajalt.clikt.output.InteractiveCliktConsole](../com.github.ajalt.clikt.output/-interactive-clikt-console/index.md) |  |
| [com.github.ajalt.clikt.core.MissingParameter](../com.github.ajalt.clikt.core/-missing-parameter/index.md) | A required parameter was not provided |
| [com.github.ajalt.clikt.core.MutuallyExclusiveGroupException](../com.github.ajalt.clikt.core/-mutually-exclusive-group-exception/index.md) |  |
| [com.github.ajalt.clikt.parameters.groups.MutuallyExclusiveOptions](../com.github.ajalt.clikt.parameters.groups/-mutually-exclusive-options/index.md) |  |
| [com.github.ajalt.clikt.output.NonInteractiveCliktConsole](../com.github.ajalt.clikt.output/-non-interactive-clikt-console/index.md) |  |
| [com.github.ajalt.clikt.core.NoRunCliktCommand](../com.github.ajalt.clikt.core/-no-run-clikt-command/index.md) | A [CliktCommand](../com.github.ajalt.clikt.core/-clikt-command/index.md) that has a default implementation of [CliktCommand.run](../com.github.ajalt.clikt.core/-clikt-command/run.md) that is a no-op. |
| [com.github.ajalt.clikt.core.NoSuchOption](../com.github.ajalt.clikt.core/-no-such-option/index.md) | An option was provided that does not exist. |
| [com.github.ajalt.clikt.parameters.options.NullableOption](../com.github.ajalt.clikt.parameters.options/-nullable-option.md) |  |
| [com.github.ajalt.clikt.parameters.options.Option](../com.github.ajalt.clikt.parameters.options/-option/index.md) | An optional command line parameter that takes a fixed number of values. |
| [com.github.ajalt.clikt.parameters.options.OptionCallTransformContext](../com.github.ajalt.clikt.parameters.options/-option-call-transform-context/index.md) | A receiver for options transformers. |
| [com.github.ajalt.clikt.parameters.options.OptionDelegate](../com.github.ajalt.clikt.parameters.options/-option-delegate/index.md) | An option that functions as a property delegate |
| [com.github.ajalt.clikt.parameters.groups.OptionGroup](../com.github.ajalt.clikt.parameters.groups/-option-group/index.md) | A group of options that can be shown together in help output, or restricted to be [cooccurring](../com.github.ajalt.clikt.parameters.groups/cooccurring.md). |
| [com.github.ajalt.clikt.parsers.OptionParser](../com.github.ajalt.clikt.parsers/-option-parser/index.md) | A parser for [Option](../com.github.ajalt.clikt.parameters.options/-option/index.md)s. |
| [com.github.ajalt.clikt.parameters.options.OptionTransformContext](../com.github.ajalt.clikt.parameters.options/-option-transform-context/index.md) | A receiver for options transformers. |
| [com.github.ajalt.clikt.parameters.options.OptionValidator](../com.github.ajalt.clikt.parameters.options/-option-validator.md) | A callback validates the final option type |
| [com.github.ajalt.clikt.parameters.options.OptionWithValues](../com.github.ajalt.clikt.parameters.options/-option-with-values/index.md) | An [Option](../com.github.ajalt.clikt.parameters.options/-option/index.md) that takes one or more values. |
| [com.github.ajalt.clikt.parsers.OptionWithValuesParser](../com.github.ajalt.clikt.parsers/-option-with-values-parser/index.md) | An option that takes one more values |
| [com.github.ajalt.clikt.parameters.groups.ParameterGroup](../com.github.ajalt.clikt.parameters.groups/-parameter-group/index.md) |  |
| [com.github.ajalt.clikt.parameters.groups.ParameterGroupDelegate](../com.github.ajalt.clikt.parameters.groups/-parameter-group-delegate/index.md) |  |
| [com.github.ajalt.clikt.core.ParameterHolder](../com.github.ajalt.clikt.core/-parameter-holder/index.md) |  |
| [com.github.ajalt.clikt.core.ParameterHolderDsl](../com.github.ajalt.clikt.core/-parameter-holder-dsl/index.md) |  |
| [com.github.ajalt.clikt.core.PrintHelpMessage](../com.github.ajalt.clikt.core/-print-help-message/index.md) | An exception that indicates that the command's help should be printed. |
| [com.github.ajalt.clikt.core.PrintMessage](../com.github.ajalt.clikt.core/-print-message/index.md) | An exception that indicates that a message should be printed. |
| [com.github.ajalt.clikt.parameters.arguments.ProcessedArgument](../com.github.ajalt.clikt.parameters.arguments/-processed-argument/index.md) | An [Argument](../com.github.ajalt.clikt.parameters.arguments/-argument/index.md) delegate implementation that transforms its values . |
| [com.github.ajalt.clikt.parameters.options.RawOption](../com.github.ajalt.clikt.parameters.options/-raw-option.md) |  |
| [com.github.ajalt.clikt.output.TermUi](../com.github.ajalt.clikt.output/-term-ui/index.md) |  |
| [com.github.ajalt.clikt.core.UsageError](../com.github.ajalt.clikt.core/-usage-error/index.md) | An internal exception that signals a usage error. |
| [com.github.ajalt.clikt.parameters.options.ValueTransformer](../com.github.ajalt.clikt.parameters.options/-value-transformer.md) | A callback that transforms a single value from a string to the value type |
| [com.github.ajalt.clikt.parameters.options.ValueWithDefault](../com.github.ajalt.clikt.parameters.options/-value-with-default/index.md) | A container for a value that can have a default value and can be manually set |
