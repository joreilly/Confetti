pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "Confetti"
include(":androidApp")
include(":shared")
include(":backend")
include(":backend:service-graphql")
include(":backend:datastore")
include(":backend:service-import")