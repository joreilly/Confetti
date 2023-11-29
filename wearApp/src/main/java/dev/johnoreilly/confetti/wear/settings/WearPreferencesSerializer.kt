package dev.johnoreilly.confetti.wear.settings

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import dev.johnoreilly.confetti.wear.proto.NetworkDetail
import dev.johnoreilly.confetti.wear.proto.NetworkPreferences
import dev.johnoreilly.confetti.wear.proto.WearPreferences
import java.io.InputStream
import java.io.OutputStream

object WearPreferencesSerializer : Serializer<WearPreferences> {
    override val defaultValue: WearPreferences = WearPreferences(
        showNetworks = NetworkDetail.NETWORK_DETAIL_NONE,
        networkPreferences = NetworkPreferences(
            allowLte = false
        )
    )

    override suspend fun readFrom(input: InputStream): WearPreferences {
        try {
            return WearPreferences.ADAPTER.decode(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: WearPreferences, output: OutputStream) = WearPreferences.ADAPTER.encode(output, t)
}

val Context.wearPreferencesStore: DataStore<WearPreferences> by dataStore(
    fileName = "preferences.pb",
    serializer = WearPreferencesSerializer,
)
