package dev.johnoreilly.confetti

import kotlinx.coroutines.runBlocking
import org.junit.Test

class MainTest {
    @Test
    fun test() {
        runBlocking {
            main()
        }
    }
}