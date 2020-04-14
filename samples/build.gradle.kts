import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "application")

    dependencies {
        "implementation"(project(":clikt"))
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
        }
    }
}
