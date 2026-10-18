@file:Suppress("UnstableApiUsage")

// Generates :wearApp's baseline profile: ./gradlew :wearApp:generateBaselineProfile
plugins {
    id("com.android.test")
    id("androidx.baselineprofile")
}

android {
    namespace = "dev.johnoreilly.confetti.benchmark"
    compileSdk = AndroidSdk.compile

    defaultConfig {
        minSdk = 28
        targetSdk = AndroidSdk.target
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":wearApp"

    testOptions.managedDevices.localDevices {
        // API 33, not 34: the API 34 Wear image's profman crashes extracting this app's profile
        // ("Tried to access vB of instruction move-result-object"). Both images also set
        // dalvik.vm.dex2oat-cpu-set=2,3 with only 2 CPUs, which BaselineProfile.kt works around.
        create("wearApi33") {
            device = "Wear OS Large Round"
            apiLevel = 33
            systemImageSource = "android-wear"
        }
    }
}

baselineProfile {
    managedDevices += "wearApi33"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.junit)
    implementation(libs.test.espressocore)
    implementation(libs.test.uiautomator)
    implementation(libs.androidx.benchmarkmacro)
}
