package test

import com.apollographql.apollo3.api.json.buildJsonString
import com.apollographql.apollo3.api.json.writeObject
import dev.johnoreilly.confetti.backend.AppHttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Test

class AppHttpHandlerTest {
    private fun conferencesAreCached(document: String, block: (String?) -> Unit) {
        val app = AppHttpHandler()

        val body = buildJsonString {
            writeObject {
                name("query")
                value(document)
            }
        }

        val response = app(Request(Method.POST, "/graphql").body(body))
        block(response.header("cache-control"))
    }

    @Test
    fun bookmarksAreNotCached() {
        @Language("graphql")
        val document = """
            query GetFoo {
                bookmarkConnection {
                    nodes {
                        id
                    }                
                }
            }
        """.trimIndent()

        conferencesAreCached(document) {
            assertEquals("no-store", it)
        }
    }

    @Test
    fun conferencesAreCached() {
        @Language("graphql")
        val document = """
            query GetFoo {
                conferences {
                    id
                }
            }
        """.trimIndent()

        conferencesAreCached(document) {
            assertEquals("public, max-age=1800", it)
        }
    }
}