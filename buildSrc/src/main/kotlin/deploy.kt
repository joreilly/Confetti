import org.gradle.api.Project
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
    val deployImageToGcp = registerBuildImageTask(name, mainClass, "deployImageToGcp", gcpServiceAccountJson)
    registerBuildImageTask(name, mainClass, "deployImageToDockerDaemon", null)

    tasks.register("bumpCloudRunRevision", BumpCloudRunRevision::class.java) {
        it.serviceName.set(name)

        it.dependsOn(deployImageToGcp)
    }
}

fun Project.registerBuildImageTask(
    imageName: String,
    mainClass: String,
    taskName: String,
    gcpServiceAccountJson: String?
): TaskProvider<BuildImageTask> {
    val isMpp = extensions.getByName("kotlin") is KotlinMultiplatformExtension
    val jarTask = if (isMpp) "jvmJar" else "jar"
    val runtimeConfiguration = if (isMpp) "jvmRuntimeClasspath" else "runtimeClasspath"

    return tasks.register(taskName, BuildImageTask::class.java) {
        it.jarFile.fileProvider(tasks.named(jarTask).map { it.outputs.files.singleFile })
        it.runtimeClasspath.from(configurations.getByName(runtimeConfiguration))
        it.mainClass.set(mainClass)
        it.imageName.set(imageName)
        if (gcpServiceAccountJson != null) {
            it.gcpServiceAccountJson.set(gcpServiceAccountJson)
        }
    }
}