package dev.johnoreilly.confetti.backend

import java.io.File

fun main(args: Array<String>) {
  val schema = buildSchema()
  File("../../shared/src/commonMain/graphql/schema.graphqls").writeText(schema.print())
}

