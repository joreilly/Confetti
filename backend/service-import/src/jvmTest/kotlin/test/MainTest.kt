package test

import dev.johnoreilly.confetti.backend.import.SwiftConnection
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test

class MainTest {
    @Test
    @Ignore
    fun test() {
        runBlocking {
            val updated = SwiftConnection.import()
            println("Updated $updated sessions")
        }
    }
}