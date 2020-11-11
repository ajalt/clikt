package com.github.ajalt.clikt.core

enum class Shell {
    BASH, ZSH, FISH;

    companion object {
        fun parse(envVar: String) = when {
            envVar.equals("fish", ignoreCase = true) -> FISH
            envVar.equals("zsh", ignoreCase = true) -> ZSH
            else -> BASH
        }
    }
}
