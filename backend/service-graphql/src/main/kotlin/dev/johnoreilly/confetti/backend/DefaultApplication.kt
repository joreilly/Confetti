@file:Suppress("unused")

package dev.johnoreilly.confetti.backend

import com.apollographql.apollo3.annotations.ApolloExperimental
import com.apollographql.apollo3.ast.toGQLDocument
import com.apollographql.apollo3.ast.toSchema
import com.apollographql.apollo3.execution.ExecutableSchema
import com.apollographql.apollo3.execution.GraphQLRequest
import com.apollographql.apollo3.execution.GraphQLRequestError
import com.apollographql.apollo3.execution.InMemoryPersistedDocumentCache
import com.apollographql.apollo3.execution.parseGetGraphQLRequest
import com.apollographql.apollo3.execution.parsePostGraphQLRequest
import com.apollographql.apollo3.tooling.SchemaUploader
import com.google.firebase.auth.FirebaseAuthException
import confetti.execution.ConfettiAdapterRegistry
import confetti.execution.ConfettiResolver
import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.graphql.DataStoreDataSource
import dev.johnoreilly.confetti.backend.graphql.TestDataSource
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okio.Buffer
import okio.buffer
import okio.source
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.UNAUTHORIZED
import org.http4k.core.then
import org.http4k.filter.CorsPolicy.Companion.UnsafeGlobalPermissive
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer


class GraphQLHandler : HttpHandler {
    companion object {
        fun ExecutableSchema(): ExecutableSchema {
            val schema = GraphQLHandler::class.java.classLoader
                .getResourceAsStream("schema.graphqls")!!
                .source()
                .buffer()
                .readUtf8()
                .toGQLDocument()
                .toSchema()

            return ExecutableSchema.Builder(schema)
                .resolver(ConfettiResolver())
                .adapterRegistry(ConfettiAdapterRegistry)
                .persistedDocumentCache(InMemoryPersistedDocumentCache())
                .addInstrumentation(CacheControlInstrumentation())
                .build()
        }
    }

    private val executableSchema = ExecutableSchema()

    override fun invoke(request: Request): Response {

        val graphQLRequestResult = when (request.method) {
            Method.GET -> request.uri.toString().parseGetGraphQLRequest()
            Method.POST -> request.body.stream.source().buffer().use { it.parsePostGraphQLRequest() }
            else -> error("")
        }

        if (graphQLRequestResult is GraphQLRequestError) {
            println("Got Request: ${graphQLRequestResult.message}")
            return Response(BAD_REQUEST).body(graphQLRequestResult.message)
        }
        graphQLRequestResult as GraphQLRequest

        println("Got Request")
        println("document=${graphQLRequestResult.document}")
        println("variables=${graphQLRequestResult.variables}")

        var conference = request.query("conference")
        if (conference == null) {
            conference = request.header("conference")
        }
        if (conference == null) {
            conference = ConferenceId.KotlinConf2023.id
        }

        val uid = try {
            request.header("authorization")
                ?.substring("bearer_".length)
                ?.firebaseUid()
        } catch (e: FirebaseAuthException) {
            return Response(UNAUTHORIZED).body(e.message ?: "Unauthorized")
        }

        if (ConferenceId.entries.find { it.id == conference } == null && conference != "all") {
            return Response(BAD_REQUEST).body("Invalid conference: '$conference'")
        }

        val source = when (conference) {
            ConferenceId.TestConference.id -> TestDataSource()
            else -> DataStoreDataSource(conference, uid)
        }

        val cacheControl = CacheControl(1800)
        var context = Source(source) + Conference(conference) + cacheControl
        if (uid != null) {
            context += UserId(uid)
        }

        val response = executableSchema.execute(graphQLRequestResult, context)

        var maxAge = cacheControl.maxAge
        if (!response.canBeCached()) {
            maxAge = 0
        }
        val cacheControlHeader = if (maxAge == 0L) {
            "no-store"
        } else {
            "public, max-age=$maxAge"
        }

        val buffer = Buffer()
        response.serialize(buffer)
        val responseText = buffer.readUtf8()
        println("repsonse: $responseText")
        return Response(OK)
            .header("content-type", "application/json")
            .header("Cache-Control", cacheControlHeader)
            .body(responseText)
    }
}

class SandboxHandler : HttpHandler {
    override fun invoke(request: Request): Response {
        return Response(OK).body(javaClass.classLoader!!.getResourceAsStream("sandbox.html")!!)
    }
}

fun AppHttpHandler(): HttpHandler {
    val app = routes(
        "/graphql" bind Method.POST to GraphQLHandler(),
        "/graphql" bind Method.GET to GraphQLHandler(),
        "/sandbox" bind Method.GET to SandboxHandler()
    )

    val errorFilter = ServerFilters.CatchAll {
        it.printStackTrace()
        ServerFilters.CatchAll.originalBehaviour(it)
    }

    return errorFilter.then(ServerFilters.Cors(UnsafeGlobalPermissive)).then(app)
}

fun runServer() {
    AppHttpHandler().asServer(SunHttp(8000)).start().also {
        maybeUploadSchema()
    }.block()
}

private fun maybeUploadSchema() {
    val apolloKey = GraphQLHandler::class.java.classLoader.getResourceAsStream("apollo.key")?.use {
        it.source().buffer().readUtf8().trim()
    }

    if (apolloKey == null) {
        println("Skipping Apollo schema upload")
        return
    }

    val graph = apolloKey.split(":").getOrNull(1)
    if (graph == null) {
        println("Cannot determine graph. Make sure to use a graph key")
        return
    }
    println("Uploading Apollo schema...")

    @OptIn(ApolloExperimental::class)
    try {
        SchemaUploader.uploadSchema(
            apolloKey = apolloKey,
            sdl = GraphQLHandler::class.java.classLoader.getResourceAsStream("schema.graphqls")!!.reader()
                .use { it.readText() },
            graph = graph,
            variant = "main",
            // Name of the subgraph as defined in Apollo Studio
            subgraph = "Confetti-current",
            revision = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString(),
        )
        println("Schema uploaded.")
    } catch (e: Exception) {
        println("Cannot upload schema: ${e.message}")

    }
}

