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
            // For Apollo Kotlin snapshots
            maven {
                url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            }
            mavenCentral()
            google()
            gradlePluginPortal()
        }
    }
}
