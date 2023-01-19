package dev.johnoreilly.confetti.backend.import

import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.StringValue
import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.datastore.DataStore
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@Suppress("UNUSED_PARAMETER")
suspend fun main(args: Array<String>) {
    println("""
        - update a conference: curl -X POST http://localhost:8080/update/droidconsf
        - update the days of a conference: curl -X POST http://localhost:8080/update-days
        - update the shortDescription: ./gradlew localRun --args updateShortDescription
    """.trimIndent())

    if (args.size > 0) {
        val command = args[0]
        when (command) {
            "updateShortDescription" -> {
                updateShortDescription()
            }
            else -> error("Unrecognized command $command")
        }
        return
    }

    embeddedServer(CIO, port = 8080) {
        install(StatusPages) {
            exception<Throwable> { call, cause ->
                cause.printStackTrace()
                call.respondText(text = "Oooopsie", status = HttpStatusCode.InternalServerError)
            }
        }
        routing {
            post("/update/{conf}") {
                update(call.parameters["conf"])
                call.respond(HttpStatusCode.OK)
            }
            post("/update-days") {
                DataStore().updateDays()
                call.respond(HttpStatusCode.OK)
            }
            post("/update-shortDescription") {
                updateShortDescription()
                call.respond(HttpStatusCode.OK)
            }
        }
    }.start(wait = true)
}

private fun Entity.getStringOrNull(name: String): String? = try {
    getString(name)
} catch (_: Exception) {
    null
}

private fun updateShortDescription() {
    DataStore().updateSessions {
        println("updating session: ${it.key.name}")
        val description = it.getStringOrNull("description") ?: return@updateSessions null

        val shortDescription = summarizeText(description) ?: return@updateSessions null

        Entity.newBuilder(it).set(
            "shortDescription", StringValue(shortDescription)
        ).build()
    }
}

private suspend fun update(conf: String?) {
    when (ConferenceId.from(conf)) {
        ConferenceId.DroidConSF2022 -> DroidConSF.import()
        ConferenceId.DevFestNantes2022 -> DevFestNantes.import()
        ConferenceId.FrenchKit2022 -> FrenchKit.import()
        ConferenceId.GraphQLSummit2022 -> GraphQLSummit.import()
        ConferenceId.DroidConLondon2022 -> Sessionize.importDroidConLondon2022()
        ConferenceId.Fosdem2023 -> Fosdem.import()
        else -> error("")
    }
}

