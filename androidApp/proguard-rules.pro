# Koin bug not resolving SavedStateHandle https://github.com/InsertKoinIO/koin/issues/1468
-keepnames class androidx.lifecycle.SavedStateHandle

# Referenced by kotlinx.datetime
-dontwarn kotlinx.serialization.KSerializer
-dontwarn kotlinx.serialization.Serializable

# Referenced by okhttp
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# Referenced by ktor
-dontwarn org.slf4j.impl.StaticLoggerBinder

-keep class com.squareup.wire.** { *; }
-keep class dev.johnoreilly.confetti.wear.proto.** { *; }
-keep class androidx.car.app.** { *; }
