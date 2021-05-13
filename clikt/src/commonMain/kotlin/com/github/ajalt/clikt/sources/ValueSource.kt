package com.github.ajalt.clikt.sources

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.*

interface ValueSource {
    data class Invocation(val values: List<String>) {
        companion object {
            /** Create a list of a single Invocation with a single value */
            fun just(value: Any?): List<Invocation> = listOf(value(value))

            /** Create an Invocation with a single value */
            fun value(value: Any?): Invocation = Invocation(listOf(value.toString()))
        }
    }

    fun getValues(context: Context, option: Option): List<Invocation>

    companion object {
        /**
         * Get a name for an option that can be useful as a key for a value source.
         *
         * The returned value is the longest option name with its prefix removed
         *
         * ## Examples
         *
         * ```
         * name(option("-h", "--help")) == "help"
         * name(option("/INPUT")) == "INPUT"
         * name(option("--new-name", "--name")) == "new-name
         * ```
         */
        fun name(option: Option): String {
            val longestName = option.longestName()
            requireNotNull(longestName) { "Option must have a name" }
            return splitOptionPrefix(longestName).second
        }

        /**
         * Create a function that will return string keys for options.
         *
         * By default, keys will be equal to the value returned by [name].
         *
         * @param prefix A static string prepended to all keys
         * @param joinSubcommands If null, keys will not include names of subcommands. If given,
         *   this string be used will join subcommand names to the beginning of keys. For options
         *   that are in a root command, this has no effect. For option in subcommands, the
         *   subcommand name will joined. The root command name is never included.
         * @param uppercase If true, returned keys will be entirely uppercase.
         * @param replaceDashes `-` characters in option names will be replaced with this character.
         */
        fun getKey(
                prefix: String = "",
                joinSubcommands: String? = null,
                uppercase: Boolean = false,
                replaceDashes: String = "-"
        ): (Context, Option) -> String = { context, option ->
            var k = name(option).replace("-", replaceDashes)
            if (joinSubcommands != null) {
                k = (context.commandNameWithParents().drop(1) + k).joinToString(joinSubcommands)
            }
            k = k.replace("-", replaceDashes)
            if (uppercase) k = k.uppercase()
            prefix + k
        }

        /**
         * Create a function that will return string keys that match the key used for environment variables.
         */
        fun envvarKey(): (Context, Option) -> String = { context, option ->
            val env = when (option) {
                is OptionWithValues<*, *, *> -> option.envvar
                is FlagOption<*> -> option.envvar
                else -> null
            }
            inferEnvvar(option.names, env, context.autoEnvvarPrefix) ?: ""
        }
    }
}
