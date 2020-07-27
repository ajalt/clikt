package com.github.ajalt.clikt.sources

@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Deprecated(message = "This opt-in requirement is not used anymore. Remove its usages from your code.")
annotation class ExperimentalValueSourceApi
