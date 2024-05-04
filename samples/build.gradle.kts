subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "application")

    dependencies {
        "implementation"(project(":clikt"))
        "implementation"(project(":clikt-mordant"))
    }
}
