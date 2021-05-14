# Upgrading to Newer Releases

## Upgrading to 3.0

### Maven Coordinates

Clikt's Maven groupId changed from `com.github.ajalt` to `com.github.ajalt.clikt`. So the full
coordinate is now `com.github.ajalt.clikt:clikt:3.0.0`.

With the new Multiplatform plugin in Kotlin 1.4, there is no longer a separate `clikt-multiplatform`
artifact. You can use `com.github.ajalt.clikt:clikt:3.0.0` for both JVM-only and Multiplatform projects.


### Environment variable splitting

There used to be an `envvarSplit` parameter to `option()` and its `convert()` that would split
values coming from an environment variable. This parameter is removed, and values from environment
variables are no longer split automatically.

If you still want to split option values, you can do so explicitly with [`split()`][split].

### Experimental APIs

The Value Source API and Completion Generation APIs no longer require opt-in. You can use these APIs
without needing the `ExperimentalValueSourceApi` or `ExperimentalCompletionCandidates` annotations.

### Localization

By default, all strings are defined in the [`Localization`][Localization] object set on your
[context][[Context.localization].

This means that string parameters like `usageTitle` in the constructor for
[`CliktHelpFormatter`][CliktHelpFormatter] have been removed in favor of functions like
[`Localization.usageTitle()`][Localization.usageTitle].

`Context.helpOptionMessage` has also been removed in favor of
[`Localization.helpOptionMessage()`][Localization.helpOptionMessage]. See [Help Option
Customization][help-option-custom] for an example.


[CliktHelpFormatter]:               api/clikt/com.github.ajalt.clikt.output/-clikt-help-formatter/index.html
[Context.localization]:             api/clikt/com.github.ajalt.clikt.core/-context/-builder/localization.html
[help-option-custom]:               documenting.md#help-option-customization
[Localization]:                     api/clikt/com.github.ajalt.clikt.output/-localization/index.html
[Localization.usageTitle]:          api/clikt/com.github.ajalt.clikt.output/-localization/usage-title.html
[Localization.helpOptionMessage]:   api/clikt/com.github.ajalt.clikt.output/-localization/help-option-message.html
[split]:                            api/clikt/com.github.ajalt.clikt.parameters.options/split.html
