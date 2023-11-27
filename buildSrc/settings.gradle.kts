dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

pluginManagement {
    listOf(repositories, dependencyResolutionManagement.repositories).forEach {
        it.apply {
            google {
                content {
                    includeGroupByRegex(".*google.*")
                    includeGroupByRegex(".*android.*")
                }
            }
            mavenCentral()
            gradlePluginPortal()
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
        }
    }
}
