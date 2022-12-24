package com.github.ajalt.clikt.internal

import com.github.ajalt.clikt.core.FileNotFound
import com.github.ajalt.clikt.mpp.readFileIfExists

internal val defaultArgFileReader: (String) -> String = { filename ->
    readFileIfExists(filename) ?: throw FileNotFound(filename)
}
