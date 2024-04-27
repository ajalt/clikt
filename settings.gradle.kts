include("clikt")
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
        // TODO: we can remove this once kotest releases a new version
        maven {
            url= uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            mavenContent { snapshotsOnly() }
        }
    }
}
