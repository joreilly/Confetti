plugins {
    `embedded-kotlin`
}

dependencies {
    implementation(platform(libs.google.cloud.bom))
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.cloud.storage)
    implementation(libs.plugin.kotlin)
    implementation(libs.plugin.android.application)
    implementation(libs.plugin.apollo)
    implementation(libs.plugin.ksp)
    implementation(libs.plugin.kmp.nativecoroutines)
    implementation(libs.plugin.kotlin.serialization)
    implementation(libs.plugin.kotlin.spring)
    implementation(libs.plugin.spring.boot)
    implementation(libs.plugin.appengine)
    implementation(libs.plugin.kmmbridge)
    implementation(libs.plugin.google.services)
    implementation(libs.plugin.firebase.crashlytics) {
        // Crashlytics depends on datastore v1.0 but we're using v1.1
        exclude(group = "androidx.datastore", module = "datastore-preferences")
    }
    implementation(libs.plugin.wire)
    implementation(libs.plugin.compose.multiplatform)
}