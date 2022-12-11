buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0-alpha09")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}")
        classpath("com.apollographql.apollo3:apollo-gradle-plugin:${Versions.apollo}")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:1.8.0-RC-1.0.8")
        classpath("com.rickclephas.kmp:kmp-nativecoroutines-gradle-plugin:${Versions.kmpNativeCoroutines}")
        classpath("org.jetbrains.kotlin:kotlin-allopen:${Versions.kotlinVersion}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Versions.kotlinVersion}")
        classpath("org.springframework.boot:spring-boot-gradle-plugin:2.5.6")
        classpath("com.google.cloud.tools:appengine-gradle-plugin:2.4.5")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://androidx.dev/storage/compose-compiler/repository")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}