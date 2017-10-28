package com.github.ajalt.clikt.parser

import com.github.ajalt.clikt.options.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.isAccessible


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
        param<PassContext> { _, _ -> PassContextParameter },
        param<IntOption> { anno, p ->
            // TODO typechecks, check name format, metavars, check that names are unique, add 'required'
            val parser = OptionParser(p.index, IntParamType)
            Option(getOptionNames(anno.names, p), parser, parser, true, anno.default, null)
        },
        param<FlagOption> { anno, p ->
            val parser = FlagOptionParser(p.index)
            Option(getOptionNames(anno.names, p), parser, parser, true, false, null)
        },
        param<IntArgument> { anno, p ->
            require(anno.nargs != 0) // TODO exceptions, check that param is a list if nargs != 1
            val default = if (anno.required || anno.nargs != 1) null else anno.default
            val name = if (anno.name.isBlank()) p.name ?: "ARGUMENT" else anno.name
            Argument(name, anno.nargs, anno.required, default, null, IntParamType, p.index)
        }
)

class Context(parent: Context?, val name: String, var obj: Any?,
              val allowInterspersedArgs: Boolean,
              internal val command: KFunction<*>,
              internal val subcommands: MutableSet<Context>,
              val parameters: List<Parameter>) {
    init {
        require(command.parameters.size == parameters.size) {
            "Incorrect number of parameters. " +
                    "(expected ${command.parameters.size}, got ${parameters.size})"
        }
    }

    var parent: Context? = parent
        internal set

    fun invoke(args: Array<Any?>) {
        command.isAccessible = true
        command.call(*args)
    }

    inline fun <reified T> findObject(): T? {
        var ctx: Context? = this
        while (ctx != null) {
            if (ctx.obj is T) return ctx.obj as T
            ctx = ctx.parent
        }
        return null
    }

    inline fun <reified T> findObject(defaultValue: () -> T): T {
        return findObject<T>() ?: defaultValue().also { obj = it }
    }

    fun findRoot(): Context {
        var ctx = this
        while (ctx.parent != null) {
            ctx = ctx.parent!!
        }
        return ctx
    }

    companion object {
        fun fromFunction(command: KFunction<*>): Context {
            val parameters = mutableListOf<Parameter>()

            // Set up long options
            for (param in command.parameters) {
                // TODO make sure instance and receiver params work
                if (param.kind != KParameter.Kind.VALUE) continue

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
            val name = command.name // TODO allow customization
            return Context(null, name, null, true, command, HashSet(), parameters)
        }
    }
}
