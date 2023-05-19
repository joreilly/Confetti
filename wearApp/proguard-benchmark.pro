# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Uncomment this to hide the original source file name.
-renamesourcefileattribute SourceFile

# Repackage classes into the top-level.
-repackageclasses

# When generating the baseline profile we want the proper names of
# the methods and classes
-dontobfuscate

# Referenced by kotlinx.datetime
-dontwarn kotlinx.serialization.KSerializer
-dontwarn kotlinx.serialization.Serializable

-keep class com.squareup.wire.** { *; }
-keep class dev.johnoreilly.confetti.wear.proto.** { *; }

# See https://github.com/firebase/firebase-android-sdk/issues/2124
-keep class com.google.android.gms.internal.** { *; }
