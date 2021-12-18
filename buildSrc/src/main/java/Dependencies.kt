
object Versions {
    const val kotlinVersion = "1.6.0"
    const val apollo = "3.0.0"

    const val kotlinCoroutines = "1.6.0-RC"

    const val compose = "1.1.0-beta04"
    const val nav_compose = "2.4.0-beta02"
    const val accompanist = "0.20.2"

    const val junit = "4.13"
}


object AndroidSdk {
    const val min = 21
    const val compile = 31
    const val target = compile
}

object Deps {
    const val apolloRuntime = "com.apollographql.apollo3:apollo-runtime:${Versions.apollo}"
}

object Compose {
    const val ui = "androidx.compose.ui:ui:${Versions.compose}"
    const val runtime = "androidx.compose.runtime:runtime:${Versions.compose}"
    const val uiGraphics = "androidx.compose.ui:ui-graphics:${Versions.compose}"
    const val uiTooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
    const val foundationLayout = "androidx.compose.foundation:foundation-layout:${Versions.compose}"
    const val material = "androidx.compose.material:material:${Versions.compose}"
    const val navigation = "androidx.navigation:navigation-compose:${Versions.nav_compose}"
    const val coilCompose = "io.coil-kt:coil-compose:1.3.1"
}



