import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

private fun Int.toJavaVersion(): String = when(this) {
    8 -> "1.8"
    else -> toString()
}
fun Project.configureCompilerOptions(jvmVersion: Int = 8) {
    tasks.withType(KotlinCompile::class.java).configureEach {
        it.compilerOptions {
            //allWarningsAsErrors.set(true)
            (this as? KotlinJvmCompilerOptions)?.let {
                it.jvmTarget.set(JvmTarget.fromTarget(jvmVersion.toJavaVersion()))
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

    extensions?.findByName("android")?.apply{
        this as BaseExtension
        compileOptions {
            it.sourceCompatibility = JavaVersion.toVersion(jvmVersion.toJavaVersion())
            it.targetCompatibility = JavaVersion.toVersion(jvmVersion.toJavaVersion())
        }
    }
}