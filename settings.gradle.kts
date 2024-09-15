include("clikt")
include("clikt-mordant")
include("clikt-mordant-markdown")
include("test")
include("samples:copy")
include("samples:repo")
include("samples:validation")
include("samples:aliases")
include("samples:helpformat")
include("samples:plugins")
include("samples:json")


@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
