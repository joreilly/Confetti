plugins {
    kotlin("android")
    id("com.android.test")
}

android {
    compileSdk = 35

    defaultConfig {
        minSdk = 29
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
        targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.majorVersion
    }

    buildTypes {
        // declare a build type (release) to match the target app's build type
        create("benchmark") {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    targetProjectPath(":androidApp")
    experimentalProperties["android.experimental.self-instrumenting"] = true

    namespace = "dev.johnoreilly.confetti.benchmark"
}

dependencies {
    implementation(libs.junit)
    implementation(libs.test.espressocore)
    implementation(libs.test.uiautomator)
    implementation(libs.androidx.benchmarkmacro)
}

androidComponents {
    beforeVariants(selector().all()) { variant ->
        variant.enable = variant.buildType == "benchmark"
    }
}
