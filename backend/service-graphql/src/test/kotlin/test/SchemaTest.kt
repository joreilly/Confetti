package test

import com.expediagroup.graphql.generator.SchemaGeneratorConfig
import com.expediagroup.graphql.generator.TopLevelObject
import com.expediagroup.graphql.generator.extensions.print
import com.expediagroup.graphql.generator.toSchema
import dev.johnoreilly.confetti.backend.CustomSchemaGeneratorHooks
import dev.johnoreilly.confetti.backend.graphql.RootQuery
import dev.johnoreilly.confetti.backend.graphqlInstantType
import graphql.schema.GraphQLDirective
import graphql.schema.GraphQLSchema
import graphql.schema.idl.SchemaPrinter
import org.junit.Test

class SchemaTest {
    @Test
    fun test() {
        val schema = toSchema(
            config = SchemaGeneratorConfig(
                supportedPackages = listOf("dev.johnoreilly.confetti.backend"),
                hooks = CustomSchemaGeneratorHooks(),
                additionalTypes = setOf(graphqlInstantType)
            ),
            queries = listOf(TopLevelObject(RootQuery(), RootQuery::class)),
            mutations = emptyList(),
            subscriptions = emptyList()
        )

        println(schema.print())
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
}