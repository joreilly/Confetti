plugins {
    id("base")
    alias(libs.plugins.composeai.preview) apply false
}

buildscript {
    dependencies {
        classpath("build-logic:build-logic")
    }
}

tasks.register("setupCredentials") {
    val gcpServiceAccountKeyFile = file("backend/datastore/src/jvmMain/resources/gcp_service_account_key.json")
    val firebaseServiceAccountKeyFile = file("backend/service-graphql/src/main/resources/firebase_service_account_key.json")
    val apolloKeyFile = file("backend/service-graphql/src/main/resources/apollo.key")
    doLast {
        fun File.writeEnv(name: String) {
            parentFile.mkdirs()
            writeText(System.getenv(name))
        }
        if (System.getenv("CI")?.isNotEmpty() == true) {
            println("setting up google services...")
            gcpServiceAccountKeyFile.writeEnv("GOOGLE_SERVICES_JSON")
            firebaseServiceAccountKeyFile.writeEnv("FIREBASE_SERVICES_JSON")
            apolloKeyFile.writeEnv("APOLLO_KEY")
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
