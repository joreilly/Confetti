package dev.johnoreilly.confetti.backend

import com.apollographql.apollo3.tooling.SchemaUploader
import com.expediagroup.graphql.apq.cache.DefaultAutomaticPersistedQueriesCache
import com.expediagroup.graphql.apq.provider.AutomaticPersistedQueriesProvider
import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.generator.toSchema
import com.expediagroup.graphql.server.execution.GraphQLRequestParser
import com.expediagroup.graphql.server.operations.Query
import com.expediagroup.graphql.server.spring.GraphQLConfigurationProperties
import com.expediagroup.graphql.server.spring.GraphQLSchemaConfiguration
import com.expediagroup.graphql.server.spring.execution.DefaultSpringGraphQLContextFactory
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLRequestParser
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLServer
import com.expediagroup.graphql.server.types.GraphQLRequest
import com.expediagroup.graphql.server.types.GraphQLResponse
import com.expediagroup.graphql.server.types.GraphQLServerRequest
import com.expediagroup.graphql.server.types.GraphQLServerResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.databind.type.TypeFactory
import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.graphql.DataStoreDataSource
import dev.johnoreilly.confetti.backend.graphql.RootQuery
import graphql.GraphQL
import graphql.GraphQLContext
import graphql.language.StringValue
import graphql.schema.*
import graphql.schema.idl.SchemaPrinter
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import okio.buffer
import okio.source
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.reactive.function.server.buildAndAwait
import org.springframework.web.reactive.function.server.coRouter
import org.springframework.web.reactive.function.server.json
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KType


@SpringBootApplication
class DefaultApplication {
    @Bean
    fun customHooks(): SchemaGeneratorHooks = CustomSchemaGeneratorHooks()

    @Bean
    @Primary
    fun schema2(
        query: Query,
        schemaConfig: SchemaGeneratorConfig
    ): GraphQLSchema {
        val schema = toSchema(
            config = schemaConfig,
            queries = listOf(TopLevelObject(query, RootQuery::class)),
            mutations = emptyList(),
            subscriptions = emptyList()
        )

        val key = javaClass.classLoader.getResourceAsStream("apollo.key.unused")?.use {
            it.source().buffer().readUtf8().trim()
        }

        //println(schema.print())

        if (key != null) {
            val graph = key.split(":").getOrNull(1)
            if (graph == null) {
                println("Cannot determine graph. Make sure to use a graph key")
            } else {
                println("Enabling Apollo reporting for graph $graph")
                SchemaUploader.uploadSchema(
                    key = key,
                    sdl = schema.print(),
                    graph = graph,
                    variant = "current"
                )
            }
        } else {
            println("Skipping Apollo reporting")
        }

        return schema
    }

    private fun GraphQLSchema.print(): String {
        return SchemaPrinter(
            SchemaPrinter.Options.defaultOptions()
                .includeIntrospectionTypes(false)
                .includeScalarTypes(true)
                .includeSchemaDefinition(false)
                .includeSchemaElement {
                    when (it) {
                        is GraphQLDirective -> !setOf(
                            "include",
                            "skip",
                            "specifiedBy",
                            "deprecated"
                        ).contains(
                            it.name
                        )

                        else -> true
                    }
                }
                .includeDirectiveDefinitions(true)
        ).print(this)
    }

//    @Bean
//    fun corsWebFilter(): CorsWebFilter {
//        val corsConfig = CorsConfiguration().apply {
//            addAllowedOrigin("*")
//            addAllowedMethod("*")
//            addAllowedHeader("*")
//        }
//
//        val source = UrlBasedCorsConfigurationSource()
//        source.registerCorsConfiguration("/**", corsConfig)
//        return CorsWebFilter(source)
//    }

    @Bean
    @Primary
    fun parser(objectMapper: ObjectMapper): GraphQLRequestParser<ServerRequest> {
        return MySpringGraphQLRequestParser(objectMapper)
    }

    @Bean
    fun graphql(graphQLSchema: GraphQLSchema): GraphQL {
        val automaticPersistedQueryProvider =
            AutomaticPersistedQueriesProvider(DefaultAutomaticPersistedQueriesCache())

        println("Configuring persisted queries")
        return GraphQL
            .newGraphQL(graphQLSchema)
            .preparsedDocumentProvider(automaticPersistedQueryProvider)
            .build()
    }

    @Bean
    fun graphQLRoutes2(
        config: GraphQLConfigurationProperties,
        graphQLServer: SpringGraphQLServer

    ) = coRouter {
        val isEndpointRequest = POST(config.endpoint) or GET(config.endpoint)
        val isNotWebSocketRequest = headers { isWebSocketHeaders(it) }.not()

        (isEndpointRequest and isNotWebSocketRequest).invoke { serverRequest ->
            val graphQLResponse = graphQLServer.execute(serverRequest)
            if (graphQLResponse != null) {
                ok().json().apply {
                    if (graphQLResponse is GraphQLResponse<*>) {
                        if (graphQLResponse.errors.orEmpty().any {
                                it.message.lowercase().contains("PersistedQueryNotFound".lowercase())
                            }) {
                            header("Cache-Control", "no-store")
                        }
                    }
                }
                    .bodyValueAndAwait(graphQLResponse)
            } else {
                badRequest().buildAndAwait()
            }
        }
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

    private fun requestContainsHeader(headers: ServerRequest.Headers, headerName: String, headerValue: String): Boolean =
        headers.header(headerName).map { it.lowercase() }.contains(headerValue.lowercase())

    companion object {
        val SOURCE_KEY = "conf"
    }
}


class MySpringGraphQLRequestParser(private val objectMapper: ObjectMapper): SpringGraphQLRequestParser(objectMapper) {
    private val mapTypeReference: MapType = TypeFactory.defaultInstance().constructMapType(HashMap::class.java, String::class.java, Any::class.java)

    override suspend fun parseRequest(serverRequest: ServerRequest): GraphQLServerRequest? {
        val graphqlRequest = super.parseRequest(serverRequest)

        if (graphqlRequest == null && serverRequest.method().equals(HttpMethod.GET)) {
            val extensions = serverRequest.queryParam("extensions").getOrNull()
            val variables = serverRequest.queryParam("variables").getOrNull()
            val operationName = serverRequest.queryParam("operationName").getOrNull()
            val graphQLVariables: Map<String, Any>? = variables?.let {
                objectMapper.readValue(it, mapTypeReference)
            }
            val graphQLExtensions: Map<String, Any>? = extensions?.let {
                objectMapper.readValue(it, mapTypeReference)
            }
            return GraphQLRequest(operationName = operationName, variables = graphQLVariables, extensions = graphQLExtensions)
        }

        return graphqlRequest
    }
}

@Component
class MyGraphQLContextFactory : DefaultSpringGraphQLContextFactory() {
    override suspend fun generateContext(request: ServerRequest): GraphQLContext {
        var conf = request.queryParam("conference").getOrNull()
        if (conf == null) {
            conf = request.headers().firstHeader("conference")
        }
        if (conf == null) {
            conf = ConferenceId.KotlinConf2023.id
        }
        val source = DataStoreDataSource(conf)

        return super.generateContext(request).put(
            DefaultApplication.SOURCE_KEY, source
        )

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


object InstantCoercing : Coercing<Instant, String> {
    override fun parseValue(input: Any): Instant = runCatching {
        Instant.parse(serialize(input))
    }.getOrElse {
        throw CoercingParseValueException("Expected valid Instant but was $input")
    }

    override fun parseLiteral(input: Any): Instant {
        val str = (input as? StringValue)?.value
        return runCatching {
            Instant.parse(str!!)
        }.getOrElse {
            throw CoercingParseLiteralException("Expected valid Instant literal but was $str")
        }
    }

    override fun serialize(dataFetcherResult: Any): String = runCatching {
        dataFetcherResult.toString()
    }.getOrElse {
        throw CoercingSerializeException("Data fetcher result $dataFetcherResult cannot be serialized to a String")
    }
}

object LocalDateCoercing : Coercing<LocalDate, String> {
    override fun parseValue(input: Any): LocalDate = runCatching {
        LocalDate.parse(serialize(input))
    }.getOrElse {
        throw CoercingParseValueException("Expected valid LocalDate but was $input")
    }

    override fun parseLiteral(input: Any): LocalDate {
        val str = (input as? StringValue)?.value
        return runCatching {
            LocalDate.parse(str!!)
        }.getOrElse {
            throw CoercingParseLiteralException("Expected valid LocalDate literal but was $str")
        }
    }

    override fun serialize(dataFetcherResult: Any): String = runCatching {
        dataFetcherResult.toString()
    }.getOrElse {
        throw CoercingSerializeException("Data fetcher result $dataFetcherResult cannot be serialized to a String")
    }
}