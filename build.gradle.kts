import org.jetbrains.compose.ComposeExtension

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
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

allprojects {
    afterEvaluate {
        extensions.findByType<ComposeExtension>()?.apply {
            kotlinCompilerPlugin.set("1.5.4-dev1-kt2.0.0-Beta1")
            kotlinCompilerPluginArgs.add("suppressKotlinVersionCompatibilityCheck=2.0.0-Beta1")
        }
    }
}
