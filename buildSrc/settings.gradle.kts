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
                forRepository { it.maven("https://storage.googleapis.com/apollo-snapshots/m2") }
                filter {
                    includeGroup("com.apollographql.execution")
                }
            }
        }
    }
}
