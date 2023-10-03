pluginManagement {
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
            mavenCentral()
            google()
            maven(url = "https://androidx.dev/storage/compose-compiler/repository")
            maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
            gradlePluginPortal {
                content {
                }
            }
            maven {
                url = uri("https://jitpack.io")
                content {
                    includeGroup("com.github.QuickBirdEng.kotlin-snapshot-testing")
                }
            }
            exclusiveContent {
                forRepository {
                    maven {
                        url = uri("https://repo.repsy.io/mvn/mbonnin/default")
                    }
                }
                filter {
                    // Use the snapshots repository for Apollo 4.0.0-dev.*, but not for 3.x, which is a dependency of 4.0.0
                    includeVersionByRegex(
                        "com\\.apollographql\\.apollo3",
                        ".+",
                        "4\\.0\\.0-dev.*"
                    )
                }
            }
            mavenLocal()
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
if (System.getenv("CONFETTI_BUILD_APPS")?.toBoolean() ?: true) {
    include(":androidApp")
    include(":androidBenchmark")
    include(":androidTest")
    include(":automotiveApp")
    include(":common:car")
    include(":shared")
    include(":landing-page")
    include(":wearApp")
    include(":wearBenchmark")
    include(":compose-desktop")
}
if (System.getenv("CONFETTI_BUILD_BACKEND")?.toBoolean() ?: true) {
    include(":backend")
    include(":backend:service-graphql")
    include(":backend:ksp-processor")
    include(":backend:datastore")
    include(":backend:service-import")
}

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    "This project needs to be run with Java 17 or higher (found: ${JavaVersion.current()})."
}
