@file:Suppress("unused")

package dev.johnoreilly.confetti.backend

import com.apollographql.federation.graphqljava.tracing.FederatedTracingInstrumentation
import com.expediagroup.graphql.apq.cache.DefaultAutomaticPersistedQueriesCache
import com.expediagroup.graphql.apq.provider.AutomaticPersistedQueriesProvider
import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.generator.toSchema
import com.expediagroup.graphql.server.execution.GraphQLRequestParser
import com.expediagroup.graphql.server.spring.GraphQLConfigurationProperties
import com.expediagroup.graphql.server.spring.execution.DefaultSpringGraphQLContextFactory
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLRequestParser
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLServer
import com.expediagroup.graphql.server.types.GraphQLRequest
import com.expediagroup.graphql.server.types.GraphQLResponse
import com.expediagroup.graphql.server.types.GraphQLServerRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.TypeFactory
import com.google.firebase.auth.FirebaseAuthException
import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.graphql.DataStoreDataSource
import dev.johnoreilly.confetti.backend.graphql.RootMutation
import dev.johnoreilly.confetti.backend.graphql.RootQuery
import dev.johnoreilly.confetti.backend.graphql.TestDataSource
import dev.johnoreilly.confetti.backend.resize.AvatarFetcher
import dev.johnoreilly.confetti.backend.resize.AvatarSize
import graphql.GraphQL
import graphql.GraphQLContext
import graphql.execution.AsyncSerialExecutionStrategy
import graphql.execution.CoercedVariables
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.*
import graphql.schema.GraphqlTypeComparatorRegistry.AS_IS_REGISTRY
import graphql.schema.idl.SchemaPrinter
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import net.mbonnin.bare.graphql.cast
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.ErrorProperties
import org.springframework.boot.autoconfigure.web.ServerProperties
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpMethod
import org.springframework.http.client.reactive.JdkClientHttpConnector
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.result.view.ViewResolver
import java.net.http.HttpClient
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KType


internal fun GraphQLSchema.print(): String {
    return SchemaPrinter(
        SchemaPrinter.Options.defaultOptions()
            .setComparators(AS_IS_REGISTRY)
            .includeIntrospectionTypes(true)
            .includeScalarTypes(true)
            .includeSchemaDefinition(true)
            .includeDirectiveDefinitions(true)
            .includeSchemaElement { true }
    ).print(this)
}

fun buildSchema(): GraphQLSchema {
    return toSchema(
        config = config,
        queries = topLevelQuery,
        mutations = topLevelMutation,
        subscriptions = emptyList()
    )
}

@SpringBootApplication
class DefaultApplication {
    @Bean
    fun schemaGeneratorConfig(): SchemaGeneratorConfig {
        return config
    }

    @Bean
    fun customHooks(): SchemaGeneratorHooks = CustomSchemaGeneratorHooks()

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

    @Bean
    @Primary
    fun parser(objectMapper: ObjectMapper): GraphQLRequestParser<ServerRequest> {
        return ApqAwareSpringGraphQLRequestParser(objectMapper)
    }

    @Bean
    fun errorWebExceptionHandler(
        errorAttributes: ErrorAttributes?,
        webProperties: WebProperties,
        serverProperties: ServerProperties,
        viewResolvers: ObjectProvider<ViewResolver?>,
        serverCodecConfigurer: ServerCodecConfigurer,
        applicationContext: ApplicationContext?
    ): ErrorWebExceptionHandler? {
        serverProperties.error.includeMessage = ErrorProperties.IncludeAttribute.ALWAYS
        val exceptionHandler = DefaultErrorWebExceptionHandler(
            errorAttributes,
            webProperties.resources,
            serverProperties.error,
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
    fun graphql(graphQLSchema: GraphQLSchema): GraphQL {
        val automaticPersistedQueryProvider =
            AutomaticPersistedQueriesProvider(DefaultAutomaticPersistedQueriesCache())

        return GraphQL
            .newGraphQL(graphQLSchema)
            .preparsedDocumentProvider(automaticPersistedQueryProvider)
            .instrumentation(CacheControlInstrumentation())
            .queryExecutionStrategy(AsyncSerialExecutionStrategy())
            .build()
    }

    @Bean
    fun routes(
        config: GraphQLConfigurationProperties,
        graphQLServer: SpringGraphQLServer,
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

        val isEndpointRequest = POST(config.endpoint) or GET(config.endpoint)
        val isNotWebSocketRequest = headers { isWebSocketHeaders(it) }.not()

        (isEndpointRequest and isNotWebSocketRequest).invoke { serverRequest ->
            try {
                var graphQLResponse = graphQLServer.execute(serverRequest)
                if (graphQLResponse != null) {
                    val headers = mutableMapOf<String, String>()
                    if (graphQLResponse is GraphQLResponse<*>) {
                        var maxAge = graphQLResponse.extensions?.get("maxAge")?.cast<Int>() ?: 0
                        if (!graphQLResponse.canBeCached()) {
                            maxAge = 0
                        }

                        if (maxAge == 0) {
                            headers.put("Cache-Control", "no-store")
                        } else {
                            headers.put("Cache-Control", "public, max-age=$maxAge")
                        }

                        var newExtensions: Map<Any, Any?>? =
                            graphQLResponse.extensions?.filterNot { it.key == "maxAge" }
                        if (newExtensions?.isEmpty() == true) {
                            newExtensions = null
                        }
                        graphQLResponse = graphQLResponse.copy(
                            extensions = newExtensions
                        )
                    }

                    ok().json()
                        .apply {
                            headers.entries.forEach {
                                header(it.key, it.value)
                            }
                        }
                        .bodyValueAndAwait(graphQLResponse)
                } else {
                    badRequest().buildAndAwait()
                }
            } catch (e: FirebaseAuthException) {
                status(401).bodyValue(e.message ?: "FirebaseAuthException").awaitSingle()
            }
        }
    }

    private fun GraphQLResponse<*>.canBeCached(): Boolean {
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

class ApqAwareSpringGraphQLRequestParser(private val objectMapper: ObjectMapper) :
    SpringGraphQLRequestParser(objectMapper) {
    private val mapTypeReference: MapType = TypeFactory.defaultInstance()
        .constructMapType(HashMap::class.java, String::class.java, Any::class.java)

    override suspend fun parseRequest(request: ServerRequest): GraphQLServerRequest? {
        val graphqlRequest = super.parseRequest(request)

        if (graphqlRequest == null && request.method().equals(HttpMethod.GET)) {
            val extensions = request.queryParam("extensions").getOrNull()
            val variables = request.queryParam("variables").getOrNull()
            val operationName = request.queryParam("operationName").getOrNull()
            val graphQLVariables: Map<String, Any>? = variables?.let {
                objectMapper.readValue(it, mapTypeReference)
            }
            val graphQLExtensions: Map<String, Any>? = extensions?.let {
                objectMapper.readValue(it, mapTypeReference)
            }
            return GraphQLRequest(
                operationName = operationName,
                variables = graphQLVariables,
                extensions = graphQLExtensions
            )
        }

        return graphqlRequest
    }
}

class BadConferenceException(conference: String) :
    Exception("Unknown conference: '$conference', use Query.conferences without a header to get the list of possible values.")

@Component
class MyGraphQLContextFactory : DefaultSpringGraphQLContextFactory() {
    override suspend fun generateContext(request: ServerRequest): GraphQLContext {
        var conference = request.queryParam("conference").getOrNull()
        if (conference == null) {
            conference = request.headers().firstHeader("conference")
        }
        if (conference == null) {
            conference = ConferenceId.KotlinConf2023.id
        }

        val uid = try {
            request.headers().firstHeader("authorization")
                ?.substring("Bearer ".length)
                ?.firebaseUid()
        } catch (e: FirebaseAuthException) {
            throw e
        }

        if (ConferenceId.entries.find { it.id == conference } == null && conference != "all") {
            throw BadConferenceException(conference)
        }
        val source = when (conference) {
            ConferenceId.TestConference.id -> TestDataSource()
            else -> DataStoreDataSource(conference, uid)
        }

        val federatedTracing =
            request.headers().firstHeader(FederatedTracingInstrumentation.FEDERATED_TRACING_HEADER_NAME) ?: "none"

        return super.generateContext(request)
            .put(DefaultApplication.KEY_SOURCE, source)
            .put(DefaultApplication.KEY_REQUEST, request)
            .put(DefaultApplication.KEY_CONFERENCE, conference)
            .put(FederatedTracingInstrumentation.FEDERATED_TRACING_HEADER_NAME, federatedTracing)
            .apply {
                if (uid != null) {
                    put(DefaultApplication.KEY_UID, uid)
                }
            }
    }
}

fun runServer(): ConfigurableApplicationContext {
    return runApplication<DefaultApplication>()
}

class CustomSchemaGeneratorHooks : SchemaGeneratorHooks {
    override fun willGenerateGraphQLType(type: KType): GraphQLType? =
        when (type.classifier as? KClass<*>) {
            Instant::class -> graphqlInstantType
            LocalDate::class -> graphqlLocalDateType
            LocalDateTime::class -> graphqlLocalDateTimeType
            else -> null
        }
}

val graphqlInstantType: GraphQLScalarType = GraphQLScalarType.newScalar()
    .name("Instant")
    .description("A type representing a formatted kotlinx.datetime.Instant")
    .coercing(InstantCoercing)
    .build()

val graphqlLocalDateType = GraphQLScalarType.newScalar()
    .name("LocalDate")
    .description("A type representing a formatted kotlinx.datetime.LocalDate")
    .coercing(LocalDateCoercing)
    .build()!!

val graphqlLocalDateTimeType = GraphQLScalarType.newScalar()
    .name("LocalDateTime")
    .description("A type representing a formatted kotlinx.datetime.LocalDateTime")
    .coercing(LocalDateTimeCoercing)
    .build()!!

object InstantCoercing : Coercing<Instant, String> {
    override fun parseValue(input: Any, graphQLContext: GraphQLContext, locale: Locale): Instant = runCatching {
        Instant.parse(input.toString())
    }.getOrElse {
        throw CoercingParseValueException("Expected valid Instant but was $input")
    }

    override fun parseLiteral(input: Value<*>, variables: CoercedVariables, graphQLContext: GraphQLContext, locale: Locale): Instant {
        val str = (input as? StringValue)?.value
        return runCatching {
            Instant.parse(str!!)
        }.getOrElse {
            throw CoercingParseLiteralException("Expected valid Instant literal but was $str")
        }
    }

    override fun serialize(dataFetcherResult: Any,  graphQLContext: GraphQLContext, locale: Locale): String = runCatching {
        dataFetcherResult.toString()
    }.getOrElse {
        throw CoercingSerializeException("Data fetcher result $dataFetcherResult cannot be serialized to a String")
    }
}

object LocalDateCoercing : Coercing<LocalDate, String> {
    override fun parseValue(input: Any, graphQLContext: GraphQLContext, locale: Locale): LocalDate = runCatching {
        LocalDate.parse(input.toString())
    }.getOrElse {
        throw CoercingParseValueException("Expected valid LocalDate but was $input")
    }

    override fun parseLiteral(input: Value<*>, variables: CoercedVariables, graphQLContext: GraphQLContext, locale: Locale): LocalDate {
        val str = (input as? StringValue)?.value
        return runCatching {
            LocalDate.parse(str!!)
        }.getOrElse {
            throw CoercingParseLiteralException("Expected valid LocalDate literal but was $str")
        }
    }

    override fun serialize(dataFetcherResult: Any,  graphQLContext: GraphQLContext, locale: Locale): String = runCatching {
        dataFetcherResult.toString()
    }.getOrElse {
        throw CoercingSerializeException("Data fetcher result $dataFetcherResult cannot be serialized to a String")
    }
}

object LocalDateTimeCoercing : Coercing<LocalDateTime, String> {
    override fun parseValue(input: Any, graphQLContext: GraphQLContext, locale: Locale): LocalDateTime = runCatching {
        LocalDateTime.parse(input.toString())
    }.getOrElse {
        throw CoercingParseValueException("Expected valid LocalDateTime but was $input")
    }

    override fun parseLiteral(input: Value<*>, variables: CoercedVariables, graphQLContext: GraphQLContext, locale: Locale): LocalDateTime {
        val str = (input as? StringValue)?.value
        return runCatching {
            LocalDateTime.parse(str!!)
        }.getOrElse {
            throw CoercingParseLiteralException("Expected valid LocalDateTime literal but was $str")
        }
    }

    override fun serialize(dataFetcherResult: Any,  graphQLContext: GraphQLContext, locale: Locale): String = runCatching {
        dataFetcherResult.toString()
    }.getOrElse {
        throw CoercingSerializeException("Data fetcher result $dataFetcherResult cannot be serialized to a String")
    }
}


internal val topLevelQuery = listOf(TopLevelObject(RootQuery(), RootQuery::class))
internal val topLevelMutation = listOf(TopLevelObject(RootMutation(), RootMutation::class))
internal val config = SchemaGeneratorConfig(
    supportedPackages = listOf("dev.johnoreilly.confetti.backend"),
    hooks = CustomSchemaGeneratorHooks(),
    additionalTypes = setOf(graphqlInstantType, graphqlLocalDateType, graphqlLocalDateTimeType)
)


@Component
class StartupApplicationListener : ApplicationListener<ApplicationReadyEvent> {
    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        initializeFirebase()
    }
}