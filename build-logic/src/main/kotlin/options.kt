import tapmoc.configureJavaCompatibility
import org.gradle.api.Project

fun Project.configureCompilerOptions(jvmVersion: Int = 17) {
    project.configureJavaCompatibility(javaVersion = jvmVersion)
}