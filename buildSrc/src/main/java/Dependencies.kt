
object Versions {
    const val kotlinVersion = "1.8.0-RC"
    const val apollo = "3.7.1"

    const val kotlinCoroutines = "1.6.4"
    const val kmpNativeCoroutines = "1.0.0-ALPHA-2"
    const val kotlinxDateTime = "0.4.0"

    const val compose = "1.4.0-alpha03"
    const val composeCompiler = "1.4.0-dev-k1.8.0-RC-4c1865595ed"
    const val navCompose = "2.5.2"
    const val accompanist = "0.28.0"
    const val composeMaterial3 = "1.0.0"
    const val activityCompose = "1.6.1"
    const val lifecycle = "2.6.0-alpha03"

    const val multiplatformSettings = "1.0.0-alpha01"
    const val koinCore = "3.2.2"
    const val koinAndroid = "3.3.0"
    const val koinAndroidCompose = "3.3.0"
    const val junit = "4.13"
}


object AndroidSdk {
    const val min = 21
    const val compile = 33
    const val target = compile
}


object Deps {
    const val multiplatformSettings = "com.russhwolf:multiplatform-settings:${Versions.multiplatformSettings}"
    const val multiplatformSettingsCoroutines = "com.russhwolf:multiplatform-settings-coroutines:${Versions.multiplatformSettings}"
    const val okhttp = "com.squareup.okhttp3:okhttp:4.9.3"
    const val graphqlKotlinSpringServer = "com.expediagroup:graphql-kotlin-spring-server:5.5.0"
    const val junit = "junit:junit:4.13.2"
    const val xoxo = "net.mbonnin.xoxo:xoxo:0.2"
    const val bareGraphQL = "net.mbonnin.bare-graphql:bare-graphql:0.0.2"
    const val kaml = "com.charleskorn.kaml:kaml:0.47.0"
    const val ktorCio = "io.ktor:ktor-server-cio:2.1.0"
    const val ktorStatusPages = "io.ktor:ktor-server-status-pages:2.1.0"
}


object Kotlinx {
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}"
    const val dateTime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.kotlinxDateTime}"
    const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2"
}

object Kotlin {
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}"
}

object Apollo {
    const val apolloRuntime = "com.apollographql.apollo3:apollo-runtime:${Versions.apollo}"
    const val apolloNormalizedCacheInMemory = "com.apollographql.apollo3:apollo-normalized-cache:${Versions.apollo}"
    const val apolloNormalizedCacheSqlite = "com.apollographql.apollo3:apollo-normalized-cache-sqlite:${Versions.apollo}"
    const val adapters = "com.apollographql.apollo3:apollo-adapters:${Versions.apollo}"
    const val tooling = "com.apollographql.apollo3:apollo-tooling:${Versions.apollo}"
}

object Compose {
    const val compiler = "androidx.compose.compiler:compiler:${Versions.composeCompiler}"
    const val ui = "androidx.compose.ui:ui:${Versions.compose}"
    const val runtime = "androidx.compose.runtime:runtime:${Versions.compose}"
    const val uiGraphics = "androidx.compose.ui:ui-graphics:${Versions.compose}"
    const val uiTooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
    const val foundationLayout = "androidx.compose.foundation:foundation-layout:${Versions.compose}"
    const val material = "androidx.compose.material:material:${Versions.compose}"
    const val materialIconsCore = "androidx.compose.material:material-icons-core:${Versions.compose}"
    const val materialIconsExtended = "androidx.compose.material:material-icons-extended:${Versions.compose}"
    const val navigation = "androidx.navigation:navigation-compose:${Versions.navCompose}"
    const val coilCompose = "io.coil-kt:coil-compose:2.0.0"

    const val activityCompose = "androidx.activity:activity-compose:${Versions.activityCompose}"
    const val lifecycleRuntimeCompose = "androidx.lifecycle:lifecycle-runtime-compose:${Versions.lifecycle}"
    const val material3 = "androidx.compose.material3:material3:${Versions.composeMaterial3}"
    const val material3WindowSizeClass = "androidx.compose.material3:material3-window-size-class:${Versions.composeMaterial3}"
    const val splashScreen = "androidx.core:core-splashscreen:1.0.0"

    const val accompanistAdaptive = "com.google.accompanist:accompanist-adaptive:${Versions.accompanist}"
    const val accompanistFlowLayout = "com.google.accompanist:accompanist-flowlayout:${Versions.accompanist}"
    const val accompanistPager = "com.google.accompanist:accompanist-pager:${Versions.accompanist}"
    const val accompanistPagerIndicator = "com.google.accompanist:accompanist-pager-indicators:${Versions.accompanist}"
}

object Koin {
    val core = "io.insert-koin:koin-core:${Versions.koinCore}"
    val test = "io.insert-koin:koin-test:${Versions.koinCore}"
    val android = "io.insert-koin:koin-android:${Versions.koinAndroid}"
    val compose = "io.insert-koin:koin-androidx-compose:${Versions.koinAndroidCompose}"
}



