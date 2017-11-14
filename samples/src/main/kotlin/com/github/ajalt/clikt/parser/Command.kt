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

    fun getFormattedHelp(): String {
        return helpFormatter.formatHelp(parameters.mapNotNull { it.parameterHelp } +
                subcommands.map { it.helpAsSubcommand() })
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
    abstract fun create(anno: T, funcParam: KParameter): Parameter

    @Suppress("UNCHECKED_CAST")
    internal fun createErased(anno: Annotation, funcParam: KParameter) = create(anno as T, funcParam)
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
    @PublishedApi internal constructor() :
            this(builtinParameters.toMutableMap(), builtinFuncParameters.toMutableMap())

    private val subcommandFunctions = mutableSetOf<KFunction<*>>()
    private val subcommands = mutableSetOf<Command>()

    var context: (Command, Context?) -> Context = { cmd, parent -> Context(parent, cmd) }

    var helpFormatter: HelpFormatterFactory = { prolog, epilog ->
        PlaintextHelpFormatter(prolog, epilog)
    }

    fun subcommand(function: KFunction<*>) {
        subcommandFunctions.add(function)
    }

    fun subcommand(command: Command) {
        subcommands.add(command)
    }


    // TODO pass the param name instead of KParameter
    inline fun <reified T : Annotation> parameter(crossinline block: (T, KParameter) -> Parameter) {
        paramsByAnnotation[T::class] = object : ParameterFactory<T>() {
            override fun create(anno: T, funcParam: KParameter) = block(anno, funcParam)
        }
    }

    inline fun <reified T : Annotation> functionAnnotation(crossinline block: (T) -> Parameter) {
        functionAnnotations[T::class] = object : FunctionParameterFactory<T>() {
            override fun create(anno: T) = block(anno)
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

        val clicktAnno = function.annotations.find { it is ClicktCommand }
        if (clicktAnno != null) {
            clicktAnno as ClicktCommand

            prolog = clicktAnno.help.trim()
            epilog = clicktAnno.epilog.trim()
            shortHelp = clicktAnno.shortHelp.trim()

            if (clicktAnno.name.isNotBlank()) name = clicktAnno.name

            if (clicktAnno.addHelpOption) {
                val helpOptionNames: List<String> = if (clicktAnno.helpOptionNames.isEmpty()) {
                    listOf("-h", "--help") // TODO: only use names that aren't taken
                } else {
                    clicktAnno.helpOptionNames.toList()
                }
                parameters.add(HelpOption(helpOptionNames))
            }
        }

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
                val p = paramsByAnnotation[anno.annotationClass]?.createErased(anno, param) ?: continue
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

    companion object {
        private inline fun <reified T : Annotation> param(crossinline block: (T, KParameter) -> Parameter):
                Pair<KClass<T>, ParameterFactory<T>> {
            return T::class to object : ParameterFactory<T>() {
                override fun create(anno: T, funcParam: KParameter) = block(anno, funcParam)
            }
        }

        private inline fun <reified T : Annotation> fparam(crossinline block: (T) -> Parameter):
                Pair<KClass<T>, FunctionParameterFactory<T>> {
            return T::class to object : FunctionParameterFactory<T>() {
                override fun create(anno: T) = block(anno)
            }
        }

        private fun getOptionNames(names: Array<out String>, param: KParameter) =
                if (names.isNotEmpty()) names.toList()
                else {
                    require(!param.name.isNullOrEmpty()) { "Cannot infer option name; specify it explicitly." }
                    listOf("--" + param.name)
                }

        // TODO allow registering new parameter types
        private val builtinParameters = mapOf(
                param<PassContext> { _, _ -> PassContextParameter() },
                param<IntOption> { anno, p ->
                    // TODO typechecks, check name format, metavars, check that names are unique, add 'required'
                    val parser = TypedOptionParser(IntParamType, anno.nargs)
                    val default = if (anno.nargs > 1) null else anno.default
                    Option(getOptionNames(anno.names, p), parser, false, default, "INT", anno.help)
                },
                param<StringOption> { anno, p ->
                    val parser = TypedOptionParser(StringParamType, anno.nargs)
                    val useDefault = anno.nargs > 1 || anno.default == STRING_OPTION_NO_DEFAULT
                    val default = if (useDefault) null else anno.default
                    Option(getOptionNames(anno.names, p), parser, false, default, "TEXT", anno.help)
                },
                param<FlagOption> { anno, p ->
                    val parser = FlagOptionParser()
                    Option(getOptionNames(anno.names, p), parser, false, false, null, anno.help)
                },
                param<IntArgument> { anno, p ->
                    require(anno.nargs != 0) // TODO exceptions, check that param is a list if nargs != 1
                    val default = if (anno.required || anno.nargs != 1) null else anno.default
                    val name = if (anno.name.isBlank()) p.name ?: "ARGUMENT" else anno.name
                    Argument(name, anno.nargs, anno.required, default, name.toUpperCase(), // TODO: better name inference
                            IntParamType, anno.help)
                },
                param<StringArgument> { anno, p ->
                    require(anno.nargs != 0) // TODO exceptions, check that param is a list if nargs != 1
                    val default = if (anno.required || anno.nargs != 1) null else anno.default
                    val name = if (anno.name.isBlank()) p.name ?: "ARGUMENT" else anno.name
                    Argument(name, anno.nargs, anno.required, default, name.toUpperCase(), // TODO: better name inference
                            StringParamType, anno.help)
                }
        )

        private val builtinFuncParameters = mapOf<KClass<out Annotation>, FunctionParameterFactory<*>>(
                fparam<AddVersionOption> { param ->
                    VersionOption(param.names.toList(), param.progName, param.version, param.message)
                }
        )
    }
}
