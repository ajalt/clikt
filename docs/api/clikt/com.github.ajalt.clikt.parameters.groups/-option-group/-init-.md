[clikt](../../index.md) / [com.github.ajalt.clikt.parameters.groups](../index.md) / [OptionGroup](index.md) / [&lt;init&gt;](./-init-.md)

# &lt;init&gt;

`OptionGroup(name: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null, help: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`? = null)`

A group of options that can be shown together in help output, or restricted to be [cooccurring](../cooccurring.md).

Declare a subclass with option delegate properties, then use an instance of your subclass is a
delegate property in your command with [provideDelegate](../provide-delegate.md).

### Example:

``` kotlin
class UserOptions : OptionGroup(name = "User Options", help = "Options controlling the user") {
  val name by option()
  val age by option().int()
}

class Tool : CliktCommand() {
  val userOptions by UserOptions()
}
```

### Note:

If you're using IntelliJ, bug KT-31319 prevents [provideDelegate](../provide-delegate.md) from being imported
automatically, so until that's fixed, you'll need to add this import manually:

`import com.github.ajalt.clikt.parameters.groups.provideDelegate`

