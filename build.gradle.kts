buildscript {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}")
        classpath("com.apollographql.apollo3:apollo-gradle-plugin:${Versions.apollo}")
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
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}