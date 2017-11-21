package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.*
import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

typealias HelpFormatterFactory = (String, String) -> HelpFormatter
typealias ContextFactory = (Command, Context?) -> Context

class Command internal constructor(val allowInterspersedArgs: Boolean,
                                   internal val function: KFunction<*>,
                                   val parameters: List<Parameter>,
                                   val name: String,
                                   private val shortHelp: String,
                                   private val contextFactory: ContextFactory,
                                   private val helpFormatter: HelpFormatter,
                                   internal val subcommands: Set<Command>) {
    init {
        require(function.parameters.size == parameters.count { it.exposeValue }) {
            "Incorrect number of parameters. " +
                    "(expected ${function.parameters.size}, got ${parameters.size})"
        }
    }

    fun parse(argv: Array<String>) {
        Parser.parse(argv, makeContext(null))
    }

    fun main(argv: Array<String>) {
        Parser.main(argv, makeContext(null))
    }

    fun getFormattedUsage(): String {
        val params = parameters.mapNotNull { it.parameterHelp } +
                subcommands.map { it.helpAsSubcommand() }
        return helpFormatter.formatUsage(params, programName = name)
    }

    fun getFormattedHelp(): String {
        val params = parameters.mapNotNull { it.parameterHelp } +
                subcommands.map { it.helpAsSubcommand() }
        return helpFormatter.formatHelp(params, programName = name)
    }

    fun makeContext(parent: Context?) = contextFactory(this, parent)

    private fun helpAsSubcommand() = ParameterHelp(listOf(name), null,
            shortHelp, ParameterHelp.SECTION_SUBCOMMANDS, true, false) // TODO optional subcommands

    companion object {
        fun build(function: KFunction<*>): Command = CommandBuilder().build(function)

        inline fun build(function: KFunction<*>, block: CommandBuilder.() -> Unit): Command
                = CommandBuilder().apply { block() }.build(function)
    }
}

@PublishedApi
internal abstract class ParameterFactory<in T : Annotation> {
    abstract fun create(anno: T, param: KParameter): Parameter

    @Suppress("UNCHECKED_CAST")
    internal fun createErased(anno: Annotation, param: KParameter) = create(anno as T, param)
}

@PublishedApi
internal abstract class FunctionParameterFactory<in T : Annotation> {
    abstract fun create(anno: T): Parameter

    @Suppress("UNCHECKED_CAST")
    internal fun createErased(anno: Annotation) = create(anno as T)
}


class CommandBuilder private constructor(
        @PublishedApi
        internal val paramsByAnnotation: MutableMap<KClass<out Annotation>, ParameterFactory<*>>,
        @PublishedApi
        internal val functionAnnotations: MutableMap<KClass<out Annotation>, FunctionParameterFactory<*>>
) {
    @PublishedApi internal constructor() : this(mutableMapOf(), mutableMapOf()) {
        addDefaultParameters()
    }

    private val subcommandFunctions = mutableSetOf<KFunction<*>>()
    private val subcommands = mutableSetOf<Command>()

    var context: (Command, Context?) -> Context = { cmd, parent -> Context(parent, cmd) }

    var helpFormatter: HelpFormatterFactory = { prolog, epilog ->
        PlaintextHelpFormatter(prolog, epilog)
    }

    /**
     * Add a function as a subcommand.
     *
     * Any annotations added with [customParameter] or [functionAnnotation] will be available to the
     * function.
     */
    fun subcommand(function: KFunction<*>) {
        subcommandFunctions.add(function)
    }

    /**
     * Add an existing command as a subcommand.
     *
     * Annotations added with [customParameter] or [functionAnnotation] will *not* be available to the
     * command.
     */
    fun subcommand(command: Command) {
        subcommands.add(command)
    }

    inline fun <reified T : Annotation> customParameter(crossinline block: (T, KParameter) -> Parameter) {
        paramsByAnnotation[T::class] = object : ParameterFactory<T>() {
            override fun create(anno: T, param: KParameter) = block(anno, param)
        }
    }

    inline fun <reified T : Annotation> functionAnnotation(crossinline block: OptionBuilder.(T) -> Unit) {
        functionAnnotations[T::class] = object : FunctionParameterFactory<T>() {
            override fun create(anno: T): Option = Option.buildWithoutParameter { block(anno) }
        }
    }

    inline fun <reified T : Annotation> option(crossinline block: OptionBuilderWithParameter.(T) -> Unit) {
        customParameter { anno: T, param -> Option.build(param) { block(anno) } }
    }

    inline fun <reified T : Annotation> optionWithoutValue(crossinline block: OptionBuilder.(T) -> Unit) {
        customParameter { anno: T, _ -> Option.buildWithoutParameter { block(anno) } }
    }

    inline fun <reified T : Annotation, reified U : Any> argument(
            type: ParamType<U>,
            crossinline block: ArgumentBuilder<U>.(T) -> Unit) {
        customParameter { anno: T, param -> Argument.build(type, param) { block(anno) } }
    }

    private fun addDefaultParameters() {
        customParameter<PassContext> { _, _ -> PassContextParameter() }

        // TODO check name format, check that names are unique
        option<IntOption> {
            typedOption(IntParamType, it.nargs)
            names = it.names
            default = if (it.nargs == 1) it.default else null
            metavar = "INT"
            help = it.help
        }
        option<StringOption> {
            typedOption(StringParamType, it.nargs)
            names = it.names
            val useDefault = it.nargs == 1 && it.default != STRING_OPTION_NO_DEFAULT
            default = if (useDefault) it.default else null
            metavar = "TEXT"
            help = it.help
        }
        option<FlagOption> {
            parser = FlagOptionParser(it.names)
            names = it.names.flatMap { it.split("/") }.filter { it.isNotBlank() }.toTypedArray()
            default = it.default
            help = it.help
            targetChecker {
                requireType<Boolean> { "parameter ${param.name ?: ""} must be of type Boolean" }
            }
        }
        option<CountedOption> {
            parser = FlagOptionParser()
            names = it.names
            default = 0
            help = it.help
            targetChecker {
                requireType<Int> { "parameter ${param.name ?: ""} must be of type Int" }
            }
            processor = { _, values -> values.size }
        }


        argument<IntArgument, Int>(IntParamType) {
            default = it.default
            name = it.name
            nargs = it.nargs
            required = it.required
        }
        argument<StringArgument, String>(StringParamType) {
            if (it.default != STRING_OPTION_NO_DEFAULT) {
                default = it.default
            }
            name = it.name
            nargs = it.nargs
            required = it.required
        }


        functionAnnotation<AddVersionOption> {
            names = it.names
            eager = true
            parser = FlagOptionParser()
            help = "Show the version and exit."
            processor = { context, values ->
                val message: String = if (it.message.isNotBlank()) it.message else {
                    val name = if (it.progName.isNotBlank()) it.progName else context.command.name
                    "$name, version ${it.version}"
                }
                if (values.lastOrNull() == true) {
                    throw PrintMessage(message)
                }
                null
            }
        }
    }

    fun build(function: KFunction<*>): Command {
        val parameters = getParameters(function)

        // Add function annotations
        function.annotations.mapNotNullTo(parameters) {
            functionAnnotations[it.annotationClass]?.createErased(it)
        }

        var name = function.name // TODO: convert case
        var prolog = ""
        var epilog = ""
        var shortHelp = ""
        var helpOptionNames = arrayOf("-h", "--help") // TODO: only use help[ option names that aren't taken

        val clicktAnno = function.annotations.find { it is ClicktCommand } as? ClicktCommand
        if (clicktAnno != null) {
            prolog = clicktAnno.help.trim()
            epilog = clicktAnno.epilog.trim()
            shortHelp = clicktAnno.shortHelp.trim()

            if (clicktAnno.name.isNotBlank()) name = clicktAnno.name

            if (!clicktAnno.addHelpOption) {
                helpOptionNames = emptyArray()
            } else if (clicktAnno.helpOptionNames.isNotEmpty()) {
                helpOptionNames = clicktAnno.helpOptionNames
            }
        }

        if (helpOptionNames.isNotEmpty()) parameters.add(helpOption(helpOptionNames))

        val subcommands = subcommands + subcommandFunctions.map {
            CommandBuilder(paramsByAnnotation, functionAnnotations).build(it)
        }
        return Command(true, function, parameters, name, shortHelp,
                context, helpFormatter(prolog, epilog), subcommands)
    }


    private fun getParameters(function: KFunction<*>): MutableList<Parameter> {
        val parameters = mutableListOf<Parameter>()
        for (param in function.parameters) {
            require(param.kind == KParameter.Kind.VALUE) {
                "Cannot invoke an unbound method. Use a free function or bound method instead. " +
                        "(MyClass::foo does not work; MyClass()::foo does)"
            }

            var foundAnno = false
            for (anno in param.annotations) {
                val p = paramsByAnnotation[anno.annotationClass]
                        ?.createErased(anno, param) ?: continue
                require(!foundAnno) {
                    "Multiple Clickt annotations on the same parameter ${param.name}"
                }
                foundAnno = true
                parameters.add(p)
            }
            require(foundAnno) {
                "No Clickt annotation found on parameter ${param.name}"
            }
        }
        return parameters
    }
}
