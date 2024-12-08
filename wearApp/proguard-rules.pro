# Koin bug not resolving SavedStateHandle https://github.com/InsertKoinIO/koin/issues/1468
-keepnames class androidx.lifecycle.SavedStateHandle

# Referenced by kotlinx.datetime
-dontwarn kotlinx.serialization.KSerializer
-dontwarn kotlinx.serialization.Serializable

-keep class com.squareup.wire.** { *; }

-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { <fields>; }

-keep class com.google.firebase.** { *; }
