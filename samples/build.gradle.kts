import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "application")

    dependencies {
        "implementation"(project(":clikt"))
    }
}
