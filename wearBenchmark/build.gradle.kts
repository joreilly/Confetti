@file:Suppress("UnstableApiUsage")

plugins {
    kotlin("android")
    id("com.android.test")
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 29
        targetSdk = 30
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildTypes {
        // declare a build type (release) to match the target app's build type
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    targetProjectPath(":wearApp")
    namespace = "dev.johnoreilly.confetti.benchmark"
    experimentalProperties["android.experimental.self-instrumenting"] = true
}

androidComponents {
    beforeVariants {
        if (it.buildType!!.contains("release") || it.buildType!!.contains("debug")) {
            it.enable = false
        }
    }
}

dependencies {
    implementation(libs.junit)
    implementation(libs.test.espressocore)
    implementation(libs.test.uiautomator)
    implementation(libs.androidx.benchmarkmacro)
}

androidComponents {
    beforeVariants(selector().all()) { variant ->
        variant.enabled = variant.buildType == "benchmark"
    }
}
