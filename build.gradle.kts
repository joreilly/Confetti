plugins {
    id("base")
}

buildscript {
    dependencies {
        classpath("build-logic:build-logic")
    }
}

tasks.register("setupCredentials") {
    fun File.writeEnv(name: String) {
        parentFile.mkdirs()
        writeText(System.getenv(name))
    }
    doLast {
        if (System.getenv("CI")?.isNotEmpty() == true) {
            println("setting up google services...")
            file("backend/datastore/src/jvmMain/resources/gcp_service_account_key.json").writeEnv("GOOGLE_SERVICES_JSON")
            file("backend/service-graphql/src/main/resources/firebase_service_account_key.json").writeEnv("FIREBASE_SERVICES_JSON")
            file("backend/service-graphql/src/main/resources/apollo.key").writeEnv("APOLLO_KEY")
        }
    }
}

tasks.register("quickChecks") {
    dependsOn(
        ":backend:service-graphql:build",
        ":backend:service-import:build",
        ":androidApp:assembleDebug",
        ":wearApp:assembleDebug",
        ":wearApp:assembleDebugAndroidTest",
    )
}

subprojects {
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "androidx.lifecycle") {
                useVersion("2.8.6")
            }
        }
    }
}
