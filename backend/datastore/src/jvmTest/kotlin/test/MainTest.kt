package test

import dev.johnoreilly.confetti.backend.datastore.DataStore
import org.junit.Test
import kotlin.test.assertEquals

class MainTest {
    @Test
    fun getBookmarks() {
        val datastore = DataStore()

        datastore.addBookmark("testUser", "androidmakers2023", "session0")
        assertEquals(setOf("session0"), datastore.readBookmarks("testUser", "androidmakers2023"))
    }
}