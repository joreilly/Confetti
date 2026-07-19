package dev.johnoreilly.confetti.utils

import android.os.Build

val isInUnitTests = Build.DEVICE == "robolectric"
