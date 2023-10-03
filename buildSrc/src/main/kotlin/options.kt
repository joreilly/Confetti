import com.android.build.gradle.BaseExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions

private fun Int.toJavaVersion(): String = when(this) {
    8 -> "1.8"
    else -> toString()
}

fun Project.configureCompilerOptions(jvmVersion: Int = 17) {
    tasks.withType(KotlinCompile::class.java).configureEach {
        it.kotlinOptions {
            (this as? KotlinJvmOptions)?.let {
                it.jvmTarget = jvmVersion.toJavaVersion()
            }
        }
    }

    project.tasks.withType(JavaCompile::class.java).configureEach {
        // Ensure "org.gradle.jvm.version" is set to "8" in Gradle metadata of jvm-only modules.
        it.options.release.set(jvmVersion)
    }

    extensions.getByName("java").apply {
        this as JavaPluginExtension
        sourceCompatibility = JavaVersion.valueOf("VERSION_${jvmVersion.toJavaVersion().replace(".", "_")}")
        targetCompatibility = JavaVersion.valueOf("VERSION_${jvmVersion.toJavaVersion().replace(".", "_")}")
    }

    extensions.findByName("android")?.apply{
        this as BaseExtension
        compileOptions {
            it.sourceCompatibility = JavaVersion.toVersion(jvmVersion.toJavaVersion())
            it.targetCompatibility = JavaVersion.toVersion(jvmVersion.toJavaVersion())
        }
    }
}