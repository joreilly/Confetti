@file:Suppress("unused")

package dev.johnoreilly.confetti.backend

import com.apollographql.apollo.api.ExecutionContext
import com.apollographql.apollo.execution.ExecutableSchema
import com.apollographql.apollo.execution.GraphQLResponse
import com.apollographql.apollo.execution.InMemoryPersistedDocumentCache
import com.example.ServiceExecutableSchemaBuilder
import com.google.firebase.auth.FirebaseAuthException
import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.graphql.DataSource
import dev.johnoreilly.confetti.backend.graphql.DataStoreDataSource
import dev.johnoreilly.confetti.backend.graphql.TestDataSource
import dev.johnoreilly.confetti.backend.resize.AvatarFetcher
import dev.johnoreilly.confetti.backend.resize.AvatarSize
import galak.apollo.reporting.ApolloReportingInstrumentation
import galak.apollo.reporting.ApolloReportingOperationContext
import galak.spring.OnRequestResult
import galak.spring.apolloSandboxRoutes
import galak.spring.galakRoutes
import okio.Buffer
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.boot.web.server.ConfigurableWebServerFactory
import org.springframework.boot.web.server.Http2
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.boot.webflux.autoconfigure.error.DefaultErrorWebExceptionHandler
import org.springframework.boot.webflux.error.ErrorAttributes
import org.springframework.boot.webflux.error.ErrorWebExceptionHandler
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.JdkClientHttpConnector
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.queryParamOrNull
import org.springframework.web.reactive.result.view.ViewResolver
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono
import java.net.http.HttpClient
import kotlin.jvm.optionals.getOrNull


@SpringBootApplication
class DefaultApplication {
    @Bean
    fun corsWebFilter(): CorsWebFilter {
        val corsConfig = CorsConfiguration().apply {
            addAllowedOrigin("*")
            addAllowedMethod("*")
            addAllowedHeader("*")
        }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfig)
        return CorsWebFilter(source)
    }

    /**
     * Emit Access-Control-Allow-Origin even when the request carries no Origin header.
     *
     * Apollo sends persisted queries as GETs, and those responses are `public, max-age=1800`, so
     * Cloud CDN caches them - under a key of {protocol, host, query string, conference header}
     * that does NOT include Origin (see backend/terraform/main.tf). [corsWebFilter] follows the
     * CORS spec and only adds the header when the request has an Origin, which browsers send and
     * the mobile apps do not. So whichever client warmed a cache entry decided, for the next 30
     * minutes, whether browsers could read it: an entry warmed by a mobile request had no
     * Access-Control-Allow-Origin, and every browser served that copy had the response blocked,
     * silently leaving the web client with no data.
     *
     * The policy above is "*" regardless of caller, so emitting it unconditionally makes every
     * cached copy valid for every client. Only fills in a header [corsWebFilter] did not already
     * set, so genuine Origin requests keep their spec-compliant handling.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun cacheSafeCorsHeaderFilter(): WebFilter = WebFilter { exchange, chain ->
        exchange.response.beforeCommit {
            exchange.response.headers.apply {
                if (getFirst(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN) == null) {
                    set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                }
            }
            Mono.empty()
        }
        chain.filter(exchange)
    }

    /**
     * Without SSL, Reactor Netty then serves both HTTP/1.1 and h2c (cleartext HTTP/2) on the same port.
     *
     * Cloud Run talks h2c to the container (see the "h2c" port in backend/terraform/main.tf). This is
     * an attempt to get QUERY requests through: over HTTP/1.1, Cloud Run's proxy resets them with
     * "reset reason: protocol error".
     */
    @Bean
    fun http2Customizer(): WebServerFactoryCustomizer<ConfigurableWebServerFactory> =
        WebServerFactoryCustomizer { factory ->
            factory.setHttp2(Http2().apply { isEnabled = true })
        }

    @Bean
    fun errorWebExceptionHandler(
        errorAttributes: ErrorAttributes,
        webProperties: WebProperties,
        viewResolvers: ObjectProvider<ViewResolver>,
        serverCodecConfigurer: ServerCodecConfigurer,
        applicationContext: ApplicationContext
    ): ErrorWebExceptionHandler? {
        // Spring Boot 4 moved the error settings from ServerProperties to WebProperties
        webProperties.error.includeMessage = ErrorProperties.IncludeAttribute.ALWAYS
        val exceptionHandler = DefaultErrorWebExceptionHandler(
            errorAttributes,
            webProperties.resources,
            webProperties.error,
            applicationContext
        )
        exceptionHandler.setViewResolvers(viewResolvers.orderedStream().toList())
        exceptionHandler.setMessageWriters(serverCodecConfigurer.writers)
        exceptionHandler.setMessageReaders(serverCodecConfigurer.readers)
        return exceptionHandler
    }


    @Bean
    fun jdkClientHttpRequestFactory(): WebClient {
        return WebClient.builder()
            .clientConnector(
                JdkClientHttpConnector(
                    HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build()
                )
            )
            .build()
    }

    @Bean
    fun imageResizer(webClient: WebClient): AvatarFetcher {
        return AvatarFetcher(webClient)
    }

    @Bean
    fun executableSchema(): ExecutableSchema {
        return ServiceExecutableSchemaBuilder()
            .persistedDocumentCache(InMemoryPersistedDocumentCache())
            .exposeServiceCapabilities(false)
            .apply {
                val apolloKey = System.getenv("APOLLO_KEY")
                if (apolloKey != null) {
                    println("Enabling Apollo Reporting")
                }
                addInstrumentation(ApolloReportingInstrumentation(apolloKey))
            }
            .build()
    }

    @Bean
    fun routes(
        executableSchema: ExecutableSchema,
        avatarFetcher: AvatarFetcher
    ) = coRouter {
        GET("/images/avatar/{conference}/{avatar}") {
            val size = AvatarSize.fromParam(it.queryParamOrNull("size"))

            avatarFetcher.resize(
                conference = it.pathVariable("conference"),
                speakerId = it.pathVariable("avatar"),
                size = size
            )
        }

        galakRoutes(
            executableSchema = executableSchema,
            onRequest = { serverRequest ->
                var conference = serverRequest.queryParam("conference").getOrNull()
                if (conference == null) {
                    conference = serverRequest.headers().firstHeader("conference")
                }
                if (conference == null) {
                    conference = ConferenceId.KotlinConf2023.id
                }

                val uid = try {
                    serverRequest.headers().firstHeader("authorization")
                        ?.substring("Bearer ".length)
                        ?.firebaseUid()
                } catch (e: FirebaseAuthException) {
                    return@galakRoutes OnRequestResult.Error(
                        status = 401,
                        message = e.message ?: "FirebaseAuthException"
                    )
                }

                if (ConferenceId.entries.find { it.id == conference } == null && conference != "all") {
                    throw BadConferenceException(conference)
                }
                val source = when (conference) {
                    ConferenceId.TestConference.id -> TestDataSource()
                    else -> DataStoreDataSource(conference, uid)
                }

                val maxAge = when (conference) {
                    "test" -> 0L
                    else -> 1800L
                }
                val maxAgeContext = MaxAgeContext(maxAge)

                OnRequestResult.Proceed(
                    UidContext(uid) +
                        SourceContext(source) +
                        ConferenceContext(conference) +
                        maxAgeContext +
                        ApolloReportingOperationContext(
                            serverRequest.headers().header("apollographql-client-name").firstOrNull(),
                            serverRequest.headers().header("apollographql-client-version").firstOrNull(),
                        )
                )
            },
            onResponse = { graphqlResponse, executionContext ->
                val maxAgeContext = executionContext[MaxAgeContext]!!

                if (maxAgeContext.maxAge == 0L || !graphqlResponse.canBeCached()) {
                    header("Cache-Control", "no-store")
                } else {
                    header("Cache-Control", "public, max-age=${maxAgeContext.maxAge}")
                }
                header("Vary", "conference")
            })

        apolloSandboxRoutes("Confetti playground")
    }

    private fun GraphQLResponse.toByteArray(): ByteArray {
        val buffer = Buffer()
        serialize(buffer)
        return buffer.readByteArray()
    }

    private fun GraphQLResponse.canBeCached(): Boolean {
        if (errors.orEmpty().any {
                it.message.lowercase()
                    .contains("PersistedQueryNotFound".lowercase())
            }) {
            return false
        }

        return true
    }

    /**
     * These headers are defined in the HTTP Protocol upgrade mechanism that identify a web socket request
     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Protocol_upgrade_mechanism
     */
    private fun isWebSocketHeaders(headers: ServerRequest.Headers): Boolean {
        val isUpgrade = requestContainsHeader(headers, "Connection", "Upgrade")
        val isWebSocket = requestContainsHeader(headers, "Upgrade", "websocket")
        return isUpgrade and isWebSocket
    }

    private fun requestContainsHeader(
        headers: ServerRequest.Headers,
        headerName: String,
        headerValue: String
    ): Boolean =
        headers.header(headerName).map { it.lowercase() }.contains(headerValue.lowercase())

    companion object {
        val KEY_UID = "uid"
        val KEY_CONFERENCE = "conference"
        val KEY_SOURCE = "source"
        val KEY_REQUEST = "request"
        val KEY_HEADERS = "headers"
    }
}


class UidContext(val uid: String?) : ExecutionContext.Element {
    companion object Key : ExecutionContext.Key<UidContext>

    override val key = Key
}

class ConferenceContext(val conference: String) : ExecutionContext.Element {
    companion object Key : ExecutionContext.Key<ConferenceContext>

    override val key = Key
}

class SourceContext(val source: DataSource) : ExecutionContext.Element {
    companion object Key : ExecutionContext.Key<SourceContext>

    override val key = Key
}

class MaxAgeContext(var maxAge: Long) : ExecutionContext.Element {
    companion object Key : ExecutionContext.Key<MaxAgeContext>

    override val key = Key
}

class BadConferenceException(conference: String) :
    Exception("Unknown conference: '$conference', use Query.conferences without a header to get the list of possible values.")

fun runServer(): ConfigurableApplicationContext {
    return runApplication<DefaultApplication>()
}

@Component
class StartupApplicationListener : ApplicationListener<ApplicationReadyEvent> {
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        initializeFirebase()
    }
}