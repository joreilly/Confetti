
pluginManagement {
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
            mavenCentral()
            google()
            maven("https://androidx.dev/storage/compose-compiler/repository")
            gradlePluginPortal {
                content {
                }
            }
        }
    }

    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                // Appengine plugin doesn't publish the marker
                "com.google.cloud.tools.appengine" -> useModule("com.google.cloud.tools:appengine-gradle-plugin:${requested.version}")
            }
        }
    }
}

rootProject.name = "Confetti"
include(":androidApp")
include(":wearApp")
include(":shared")
