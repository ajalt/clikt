package com.github.ajalt.clikt.core

/**
 * Register an [AutoCloseable] to be closed when this command and all its subcommands have
 * finished running.
 *
 * This is useful for resources that need to be shared across multiple commands. For resources
 * that aren't shared, it's often simpler to use [use] directly.
 *
 * Registered closeables will be closed in the reverse order that they were registered.
 *
 * ### Example
 *
 * ```
 * currentContext.obj = currentContext.registerCloseable(File("foo").bufferedReader())
 * ```
 *
 * @return the closeable that was registered
 * @see Context.callOnClose
 * @see Context.registerCloseable
 */
fun <T : AutoCloseable> Context.registerJvmCloseable(closeable: T): T {
    callOnClose { closeable.close() }
    return closeable
}
