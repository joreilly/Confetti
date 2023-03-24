package test

import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.FullEntity
import com.google.cloud.datastore.StringValue
import dev.johnoreilly.confetti.backend.datastore.DataStore
import dev.johnoreilly.confetti.backend.datastore.DataStore.Companion.getListOrNull
import dev.johnoreilly.confetti.backend.datastore.DataStore.Companion.toValue
import org.junit.Test

class MainTest {
    @Test
    fun test() {
        val dataStore = DataStore()

        val join = mutableListOf<Pair<String, String>>()
        dataStore.forEachSession {
            val id = it.key.name
            val speakers = it.getListOrNull<StringValue>("speakers")
            if (speakers == null) {
                return@forEachSession
            }

            speakers.forEach {
                join.add(it.get() to id)
            }
        }

        val map = join.groupBy(
            { it.first },
            { it.second },
        )

        println("found ${map.size} sessions")
        dataStore.updateSpeakers {
            Entity.newBuilder(it).set(
                "sessions", map.get(it.key.name).orEmpty().toValue()
            ).build()
        }
    }
}