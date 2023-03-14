@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.kotlin.multiplatform).apply(false)
    alias(libs.plugins.apollo).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.kmp.nativecoroutines).apply(false)
    alias(libs.plugins.kotlin.serialization).apply(false)
    alias(libs.plugins.kotlin.spring).apply(false)
    alias(libs.plugins.spring.boot).apply(false)
    alias(libs.plugins.appengine).apply(false)
    alias(libs.plugins.kmmbridge).apply(false)
    alias(libs.plugins.google.services).apply(false)
    alias(libs.plugins.firebase.crashlytics).apply(false)
    alias(libs.plugins.wire).apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.register("setupCredentials") {
    doLast {
        if (System.getenv("CI")?.isNotEmpty() == true) {
            println("setting up google services...")
            file("backend/datastore/src/jvmMain/resources/gcp_service_account_key.json").writeText(System.getenv("GOOGLE_SERVICES_JSON"))
            file("backend/service-graphql/src/main/resources/firebase_service_account_key.json").writeText(System.getenv("FIREBASE_SERVICES_JSON"))
            file("backend/service-graphql/src/main/resources/apollo.key").writeText(System.getenv("APOLLO_KEY"))
        }
    }
}