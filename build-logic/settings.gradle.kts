// Explicit root project name for the included build. Without it Gradle warns that the
// generated type-safe project accessors depend on the checkout folder name, which changes
// the build-logic buildscript classpath across machines/CI and breaks build-cache reuse.
rootProject.name = "build-logic"

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
                    includeVersionByRegex("com.apollographql.execution", ".*", ".*SNAPSHOT.*")
                }
            }
        }
    }
}
