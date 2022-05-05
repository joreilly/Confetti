package fr.androidmakers.server

import com.expediagroup.graphql.generator.hooks.SchemaGeneratorHooks
import graphql.language.StringValue
import graphql.schema.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import kotlin.reflect.KClass
import kotlin.reflect.KType

@SpringBootApplication
class DefaultApplication {
  @Bean
  fun customHooks(): SchemaGeneratorHooks = CustomSchemaGeneratorHooks()
}

fun runServer(): ConfigurableApplicationContext {
  return runApplication<DefaultApplication>()
}

class CustomSchemaGeneratorHooks : SchemaGeneratorHooks {
  override fun willGenerateGraphQLType(type: KType): GraphQLType? = when (type.classifier as? KClass<*>) {
    Instant::class -> graphqlInstantType
    else -> null
  }
}

val graphqlInstantType = GraphQLScalarType.newScalar()
    .name("Instant")
    .description("A type representing a formatted kotlinx.datetime.Instant")
    .coercing(InstantCoercing)
    .build()

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