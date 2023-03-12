repositories {
    mavenCentral()
}

plugins {
    `embedded-kotlin`
}

dependencies {
    implementation(platform(libs.google.cloud.bom))
    implementation(libs.google.cloud.storage)
}