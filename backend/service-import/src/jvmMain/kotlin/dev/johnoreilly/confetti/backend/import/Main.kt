package dev.johnoreilly.confetti.backend.import

import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.system.exitProcess

@Suppress("UNUSED_PARAMETER")
fun main(args: Array<String>) {
    val confToUpdate = System.getenv("CONF_TO_UPDATE")
    if (!confToUpdate.isNullOrBlank()) {
        update(confToUpdate)
        exitProcess(0)
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
            post("/update/droidconsf") {
                DroidConSF.import()
                call.respond(HttpStatusCode.OK)
            }
        }
    }.start(wait = true)
}

private fun update(conf: String?) {
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