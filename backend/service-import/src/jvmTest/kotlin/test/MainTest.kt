package test

import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.FullEntity
import com.google.cloud.datastore.StringValue
import dev.johnoreilly.confetti.backend.datastore.DataStore
import dev.johnoreilly.confetti.backend.datastore.DataStore.Companion.getListOrNull
import dev.johnoreilly.confetti.backend.datastore.DataStore.Companion.toValue
import dev.johnoreilly.confetti.backend.import.SwiftConnection
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test

class MainTest {
    @Test
    fun test() {
        runBlocking {
            val updated = SwiftConnection.import()
            println("Updated $updated sessions")
        }
    }
}