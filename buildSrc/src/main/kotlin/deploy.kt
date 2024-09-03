import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Created manually
val gcpProjectName = "confetti-349319"

// Kepp in sync with terraform/main.tf
val gcpRegion = "us-west1"
val gcpServiceAccountJson by lazy {
    System.getenv("GOOGLE_SERVICES_JSON") ?: error("GOOGLE_SERVICES_JSON env variable is needed to deploy")
}

/**
 * @param name name used for the image. Also used to name the cloud run service
 */
fun Project.configureDeploy(name: String, mainClass: String) {
    val isMpp = extensions.getByName("kotlin") is KotlinMultiplatformExtension
    val jarTask = if (isMpp) "jvmJar" else "jar"
    val runtimeConfiguration = if (isMpp) "jvmRuntimeClasspath" else "runtimeClasspath"
    val jarFile = objects.fileProperty().apply {
        fileProvider(tasks.named(jarTask).map { it.outputs.files.singleFile })
    }
    val deployImageToGcp = registerBuildImageTask(
        taskName = "deployImageToGcp",
        imageName = provider { name },
        gcpServiceAccountJson = provider { gcpServiceAccountJson },
        mainClass = provider { mainClass },
        jarFile = jarFile,
        runtimeClasspath = configurations.getByName(runtimeConfiguration)
    )

    val bumCloudRunRevision = registerBumpCloudRunRevisionTask(serviceName = provider { name })

    bumCloudRunRevision.configure {
        it.dependsOn(deployImageToGcp)
    }
}
