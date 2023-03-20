package dev.johnoreilly.confetti.settings

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import dev.johnoreilly.confetti.wear.proto.WearSettings
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object WearSettingsSerializer: Serializer<WearSettings> {
    override val defaultValue: WearSettings = WearSettings()

    override suspend fun readFrom(input: InputStream): WearSettings {
        return try {
            WearSettings.ADAPTER.decode(input)
        } catch (ioe: IOException) {
            throw CorruptionException("failed to read settings", ioe)
        }
    }

    override suspend fun writeTo(t: WearSettings, output: OutputStream) {
        WearSettings.ADAPTER.encode(output, t)
    }
}