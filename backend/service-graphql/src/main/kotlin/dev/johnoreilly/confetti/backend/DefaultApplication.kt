package dev.johnoreilly.confetti.backend

import com.apollographql.apollo3.tooling.SchemaUploader
import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import com.expediagroup.graphql.generator.toSchema
import com.expediagroup.graphql.server.operations.Query
import com.expediagroup.graphql.server.spring.execution.DefaultSpringGraphQLContextFactory
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContextFactory
import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.graphql.DataStoreDataSource
import dev.johnoreilly.confetti.backend.graphql.RootQuery
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
import org.springframework.stereotype.Component
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import org.springframework.web.reactive.function.server.ServerRequest
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KClass
import kotlin.reflect.KType


@SpringBootApplication
class DefaultApplication {
    @Bean
    fun customHooks(): SchemaGeneratorHooks = CustomSchemaGeneratorHooks()

    @Bean
    fun schema(
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

        println(schema.print())

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

    companion object {
        val SOURCE_KEY = "conf"
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