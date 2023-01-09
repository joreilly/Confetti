package dev.johnoreilly.confetti.backend.import

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
        call.respondText(text = "Oooopsie" , status = HttpStatusCode.InternalServerError)
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
  when (conf) {
    "droidconsf" -> DroidConSF.import()
    "devfestnantes" -> DevFestNantes.import()
    "frenchkit2022" -> FrenchKit.import()
    "graphqlsummit2022" -> GraphQLSummit.import()
    "droidconlondon2022" -> Sessionize.importDroidConLondon2022()
    "fosdem2023" -> Fosdem.import()
    else -> error("")
  }
}