import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


fun Project.configureCompilerOptions(jvmVersion: Int = 8) {
    tasks.withType(KotlinCompile::class.java).configureEach {
        it.compilerOptions {
            //allWarningsAsErrors.set(true)
            (this as? KotlinJvmOptions)?.let {
                it.jvmTarget = when (jvmVersion) {
                    8 -> "1.8"
                    else -> jvmVersion.toString()
                }
            }
        }
    }

    project.tasks.withType(JavaCompile::class.java).configureEach {
        // Ensure "org.gradle.jvm.version" is set to "8" in Gradle metadata of jvm-only modules.
        it.options.release.set(jvmVersion)
    }

    extensions.getByName("java").apply {
        this as JavaPluginExtension
        toolchain {
            it.languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}