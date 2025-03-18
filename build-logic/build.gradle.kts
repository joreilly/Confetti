import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin19)
    alias(libs.plugins.ksp19)
    alias(libs.plugins.gratatouille)
}

dependencies {
    implementation(gradleApi())
    implementation(platform(libs.google.cloud.bom))
    implementation(platform(libs.firebase.bom))
    implementation(libs.plugin.kotlin)
    implementation(libs.plugin.android.application)
    implementation(libs.plugin.apollo)
    implementation(libs.apollo.execution.gradle.plugin)
    implementation(libs.plugin.ksp)
    implementation(libs.plugin.kotlin.serialization)
    implementation(libs.plugin.kotlin.spring)
    implementation(libs.plugin.spring.boot)
    implementation(libs.plugin.google.services)
    implementation(libs.plugin.firebase.crashlytics) {
        // Crashlytics depends on datastore v1.0 but we're using v1.1
        exclude(group = "androidx.datastore", module = "datastore-preferences")
    }
    // Force a newer version of tink in the classpath for compatibility with protobuf-java:4.x
    // Removes java.lang.NoSuchMethodError: 'void com.google.crypto.tink.proto.Keyset.makeExtensionsImmutable()'
    implementation(libs.tink)
    implementation(libs.plugin.wire)
    implementation(libs.plugin.compose.multiplatform)
    implementation(libs.jib.core)
    implementation(libs.google.cloud.storage)
    implementation(libs.google.cloud.run)
    implementation(libs.kotlinx.datetime)
    implementation(libs.roborazzi.gradle.plugin)
}

group = "build-logic"
tasks.withType(JavaCompile::class.java).configureEach {
    this.options.release.set(17)
}

tasks.withType(KotlinCompile::class.java).configureEach {
    this.kotlinOptions.jvmTarget = "17"
}
