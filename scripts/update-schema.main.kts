#!/usr/bin/env kotlin

@file:Repository("https://s01.oss.sonatype.org/content/repositories/snapshots")
@file:Repository("https://repo.maven.apache.org/maven2")
@file:DependsOn("com.github.ajalt.clikt:clikt-jvm:3.4.0")
@file:DependsOn("com.squareup.okio:okio-jvm:3.1.0")
@file:DependsOn("com.apollographql.apollo3:apollo-compiler:3.2.2")
@file:DependsOn("com.apollographql.apollo3:apollo-gradle-plugin-external:3.3.1-SNAPSHOT")

import com.apollographql.apollo3.ast.toUtf8
import com.apollographql.apollo3.compiler.introspection.toGQLDocument
import com.apollographql.apollo3.compiler.introspection.toIntrospectionSchema
import com.apollographql.apollo3.gradle.internal.SchemaDownloader
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import okio.buffer
import okio.source
import java.io.File

/**
 * Executes the given command and returns stdout as a String
 * Throws if the exit code is not 0
 */
fun executeCommand(vararg command: String): String {
    val process = ProcessBuilder()
        .command(*command)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .start()

    val output = process.inputStream.source().buffer().readUtf8()

    val exitCode = process.waitFor()
    if (exitCode != 0) {
        error("Command ${command.joinToString(" ")} failed with exitCode '$exitCode'\n" +
                "output was: $output")
    }
    return output
}

class MainCommand: CliktCommand(
    help = """Updates your GraphQL schema and creates a pull request if needed"""
) {
    val schema by option(
        help = """
            The path where to downloaded the schema.
        """.trimIndent()
    ).required()

    val headers by option(
        help = """
            Headers to use when introspecting the endpoint. Can be set multiple times. 
        """.trimIndent()
    ).multiple()

    val insecure by option(
        help = """
            Allows unsecure certificates. 
        """.trimIndent()
    ).flag()

    val endpoint by option(
        help = """
            The GraphQL schema to introspect.
        """.trimIndent()
    ).required()

    val branch by option(
        help = """
            The branch used to create the PR.
        """.trimIndent()
    ).default("update-schema")

    val remote by option(
        help = """
            The remote to push the branch used to create the PR.
        """.trimIndent()
    ).default("origin")

    override fun run() {
        val schemaStr = SchemaDownloader.downloadIntrospection(
            endpoint = endpoint,
            headers = headers.map {
                val c = it.split(":")
                check (c.size == 2) {
                    "Bad header: $it"
                }
                c[0].trim() to c[1].trim()
            }.toMap(),
            insecure = insecure, // Uncomment when using Apollo 3.3
        )

        if (schema.endsWith("json")) {
            File(schema).writeText(schemaStr)
        } else {
            val gqlSchema = schemaStr.toIntrospectionSchema().toGQLDocument()
            File(schema).writeText(gqlSchema.toUtf8(indent = "  "))
        }

        val gitCleanOutput = executeCommand("git", "status")
        if (gitCleanOutput.contains("nothing to commit, working tree clean")) {
            println("The schema did not change, exiting.")
            return
        }

        executeCommand("git", "checkout", "-b", branch)
        executeCommand("git", "commit", "-a", "-m", "update schema")
        executeCommand("git", "push", remote, branch)
        executeCommand("gh", "pr", "create", "-f")
    }
}

MainCommand().main(args)