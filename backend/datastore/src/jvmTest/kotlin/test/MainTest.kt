package test

import com.google.cloud.datastore.Query
import com.google.cloud.datastore.StructuredQuery
import dev.johnoreilly.confetti.backend.datastore.DataStore
import dev.johnoreilly.confetti.backend.datastore.DataStore.Companion.KIND_SPEAKER
import dev.johnoreilly.confetti.backend.datastore.initDatastore
import org.junit.Test

class MainTest {
    @Test
    fun checkSpeakerNames() {
        val datastore = initDatastore()

        val query = Query.newEntityQueryBuilder()
            .setKind(KIND_SPEAKER)
            .setLimit(100)
            .setFilter(
                StructuredQuery.PropertyFilter.eq("")
                    keyFactory.setKind(DataStore.KIND_CONF).newKey(conf)
                )
            )
    }
}