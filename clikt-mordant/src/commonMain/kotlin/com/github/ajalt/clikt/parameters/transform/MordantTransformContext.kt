package com.github.ajalt.clikt.parameters.transform

import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.OptionTransformContext
import com.github.ajalt.mordant.rendering.Theme
import com.github.ajalt.mordant.terminal.Terminal

/** The terminal for the current context */
val TransformContext.terminal: Terminal get() = context.terminal

/** The theme for the current context */
val TransformContext.theme: Theme get() = terminal.theme

/** The terminal from the current context */
val OptionTransformContext.terminal: Terminal get() = context.terminal
