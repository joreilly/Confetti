# Koin bug not resolving SavedStateHandle https://github.com/InsertKoinIO/koin/issues/1468
-keepnames class androidx.lifecycle.SavedStateHandle

# Referenced by kotlinx.datetime
-dontwarn kotlinx.serialization.KSerializer
-dontwarn kotlinx.serialization.Serializable

# Referenced by ktor
-dontwarn org.slf4j.impl.StaticLoggerBinder

# ktor's IntellijIdeaDebugDetector references java.lang.management, which is absent on Android.
-dontwarn java.lang.management.**

-keep class com.squareup.wire.** { *; }
-keep class dev.johnoreilly.confetti.wear.proto.** { *; }
-keep class androidx.car.app.** { *; }

-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { <fields>; }

-keep class com.google.firebase.** { *; }

-dontwarn okhttp3.internal.Util
-dontwarn io.ktor.client.network.sockets.SocketTimeoutException
