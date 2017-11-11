package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.*
import com.github.ajalt.clikt.parser.HelpFormatter.ParameterHelp
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter


private inline fun <reified T : Annotation> param(crossinline block: (T, KParameter) -> Parameter):
        Pair<KClass<T>, ParameterFactory<T>> {
    return T::class to object : ParameterFactory<T>() {
        override fun create(anno: T, funcParam: KParameter) = block(anno, funcParam)
    }
}

private fun getOptionNames(names: Array<out String>, param: KParameter) =
        if (names.isNotEmpty()) names.toList()
        else {
            require(!param.name.isNullOrEmpty()) { "Cannot infer option name; specify it explicitly." }
            listOf("--" + param.name)
        }

// TODO allow registering new parameter types
private val builtinParameters = mapOf<KClass<out Annotation>, ParameterFactory<*>>(
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
        }
)

private val defaultContextFactory: (Command, Context?) -> Context =
        { cmd, parent -> Context(parent, cmd, null) }

class Command(val allowInterspersedArgs: Boolean,
              internal val function: KFunction<*>,
              val parameters: List<Parameter>,
              val name: String,
              val prolog: String,
              val epilog: String,
              val shortHelp: String,
              private val contextFactory: (Command, Context?) -> Context) {
    init {
        require(function.parameters.size == parameters.count { it.exposeValue }) {
            "Incorrect number of parameters. " +
                    "(expected ${function.parameters.size}, got ${parameters.size})"
        }
    }

    internal val subcommands = mutableSetOf<Command>()

    fun parse(argv: Array<String>) {
        Parser.parse(argv, makeContext(null))
    }

    fun main(argv: Array<String>) {
        Parser.main(argv, makeContext(null))
    }

    /**
     * Add a command as a subcommand and return this instance.
     *
     * Alias for [addSubcommand].
     */
    fun withSubcommand(subcommand: Command) = addSubcommand(subcommand)

    /**
     * Add a command as a subcommand and return this instance.
     *
     * Alias for [addSubcommand].
     */
    fun withSubcommand(function: KFunction<*>) = addSubcommand(function)

    /** Add a command as a subcommand and return this instance. */
    fun addSubcommand(subcommand: Command) = apply { subcommands.add(subcommand) }

    /** Add a function as a subcommand and return this instance. */
    fun addSubcommand(function: KFunction<*>) = apply {
        subcommands.add(Command.fromFunction(function))
    }

    fun getFormattedHelp(): String {
        return PlaintextHelpFormatter(prolog, epilog)
                .formatHelp(parameters.mapNotNull { it.parameterHelp } +
                        subcommands.map { it.helpAsSubcommand() })
    }

    fun makeContext(parent: Context?) = contextFactory(this, parent)

    private fun helpAsSubcommand() = ParameterHelp(listOf(name), null,
            shortHelp, ParameterHelp.SECTION_SUBCOMMANDS, true, false) // TODO optional subcommands

    companion object {
        fun fromFunction(function: KFunction<*>): Command {
            val parameters = getParameters(function)
            addParametersFromFunctionAnnotations(parameters, function)

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

            return Command(true, function, parameters, name, prolog, epilog, shortHelp,
                    defaultContextFactory)
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
                    val p = builtinParameters[anno.annotationClass]?.createErased(anno, param) ?: continue
                    require(!foundAnno) {
                        "Multiple Clickt annotations on the same parameter at index ${param.index}"
                    }
                    foundAnno = true
                    parameters.add(p)
                }
                require(foundAnno) {
                    "No Clickt annotation found on parameter at index ${param.index}"
                }
            }
            return parameters
        }

        private fun addParametersFromFunctionAnnotations(parameters: MutableList<Parameter>,
                                                         function: KFunction<*>) {
            for (param in function.annotations) {
                when (param) {
                    is AddVersionOption -> {
                        parameters.add(VersionOption(param.names.toList(), param.progName,
                                param.version, param.message))
                    }
                }
            }
        }
    }
}
