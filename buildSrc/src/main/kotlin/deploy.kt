import org.gradle.api.Project

// Created manually
val gcpProjectName = "confetti-349319"
// Kepp in sync with terraform/main.tf
val gcpArtifactRegistryRepository = "graphql-images"
// Kepp in sync with terraform/main.tf
val gcpRegion = "us-west1"
val gcpServiceAccountJson by lazy {
    System.getenv("GOOGLE_SERVICES_JSON") ?: error("GOOGLE_SERVICES_JSON env variable is needed to deploy")
}

/**
 * @param name name used for the image. Also used to name the cloud run service
 */
fun Project.configureDeploy(name: String, mainClass: String) {
    registerBuildImageTask(name, mainClass, "deployImageToGcp", gcpServiceAccountJson)
    registerBuildImageTask(name, mainClass,  "deployImageToDockerDaemon", null)

    tasks.register("bumpCloudRunRevision", BumpCloudRunRevision::class.java) {
        it.imageName.set(name)
    }
}

fun Project.registerBuildImageTask(imageName: String, mainClass: String, taskName: String, gcpServiceAccountJson: String?) {
    tasks.register(taskName, BuildImageTask::class.java) {
        it.jarFile.fileProvider(tasks.named("jar").map { it.outputs.files.singleFile })
        it.runtimeClasspath.from(configurations.getByName("runtimeClasspath"))
        it.mainClass.set(mainClass)
        it.imageName.set(imageName)
        if (gcpServiceAccountJson != null) {
            it.gcpServiceAccountJson.set(gcpServiceAccountJson)
        }
    }
}