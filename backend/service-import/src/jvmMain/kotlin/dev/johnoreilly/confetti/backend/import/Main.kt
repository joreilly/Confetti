package dev.johnoreilly.confetti.backend.import

import DevfestLille
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.StringValue
import dev.johnoreilly.confetti.backend.datastore.ConferenceId
import dev.johnoreilly.confetti.backend.datastore.DataStore
import dev.johnoreilly.confetti.backend.import.Sessionize.importDevFestGeorgia2023
import dev.johnoreilly.confetti.backend.import.Sessionize.importDevFestMelbourne2023
import dev.johnoreilly.confetti.backend.import.Sessionize.importDevFestVenice2024
import dev.johnoreilly.confetti.backend.import.Sessionize.importDevFestVenice2025
import dev.johnoreilly.confetti.backend.import.Sessionize.importDroidconLisbon2023
import dev.johnoreilly.confetti.backend.import.Sessionize.importDroidconLisbon2024
import dev.johnoreilly.confetti.backend.import.Sessionize.importDroidconLondon2024
import dev.johnoreilly.confetti.backend.import.Sessionize.importDroidconLondon2025
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

@Suppress("UNUSED_PARAMETER")
suspend fun main(args: Array<String>) {
    println(
        """
        - update a conference: curl -X POST http://localhost:8080/update/droidconsf
        - update the days of a conference: curl -X POST http://localhost:8080/update-days
        - update the shortDescription: ./gradlew localRun --args updateShortDescription
    """.trimIndent()
    )

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
                val result = update(call.parameters["conf"])
                call.respondText("$result sessions updated", status = HttpStatusCode.OK)
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

fun updateSpeakers() {
    DataStore().updateSessions {
        println("updating session: ${it.key.name}")
        val description = it.getStringOrNull("description") ?: return@updateSessions null

        val shortDescription = summarizeText(description) ?: return@updateSessions null

        Entity.newBuilder(it).set(
            "shortDescription", StringValue(shortDescription)
        ).build()
    }
}

private suspend fun update(conf: String?): Int {
    return when (ConferenceId.from(conf)) {
        ConferenceId.DroidConSF2022 -> DroidConSF.import()
        ConferenceId.DevFestNantes2022 -> importDefvestNantes2022()
        ConferenceId.DevFestNantes2023 -> importDefvestNantes2023()
        ConferenceId.DevFestNantes2024 -> importDefvestNantes2024()
        ConferenceId.DevFestNantes2025 -> importDefvestNantes2025()
        ConferenceId.FrenchKit2022 -> FrenchKit.import()
        ConferenceId.GraphQLSummit2022 -> GraphQLSummit.import()
        ConferenceId.DroidConLondon2022 -> Sessionize.importDroidConLondon2022()
        ConferenceId.Fosdem2023 -> Fosdem.import()
        ConferenceId.KotlinConf2023 -> Sessionize.importKotlinConf2023()
        ConferenceId.AndroidMakers2023 -> Sessionize.importAndroidMakers2023()
        ConferenceId.FlutterConnection2023 -> TechConnection.importFlutter2023()
        ConferenceId.ReactNativeConnection2023 -> TechConnection.importReactNative2023()
        ConferenceId.DroidconSF2023 -> Sessionize.importDroidconSF2023()
        ConferenceId.DroidconBerlin2023 -> Sessionize.importDroidconBerlin2023()
        ConferenceId.TestConference -> error("The test Conference cannot be updated")
        ConferenceId.DroidconNYC2023 -> Sessionize.importDroidconNYC2023()
        ConferenceId.DroidconNYC2025 -> Sessionize.importDroidconNYC2025()
        ConferenceId.SwiftConnection2023 -> SwiftConnection.import()
        ConferenceId.DroidConLisbon2023 -> importDroidconLisbon2023()
        ConferenceId.DevFestGeorgia2023 -> importDevFestGeorgia2023()
        ConferenceId.DevFestMelbourne2023 -> importDevFestMelbourne2023()
        ConferenceId.GraphQLSummit2023 -> GraphQLSummit2023.import()
        ConferenceId.DroidconLondon2023 -> Sessionize.importDroidconLondon2023()
        ConferenceId.DevFestWarsaw2023 -> Sessionize.importDevFestWarsaw2023()
        ConferenceId.DevFestIreland2023 -> Sessionize.importDevFestIreland2023()
        ConferenceId.DevFestVenice2023 -> Sessionize.importDevFestVenice2023()
        ConferenceId.DevFestStockholm2023 -> Sessionize.importDevFestStockholm2023()
        ConferenceId.DevFestSriLanka2023 -> Sessionize.importDevFestSriLanka2023()
        ConferenceId.DroidconAmman2024 -> Sessionize.importDroidconAmman2024()
        ConferenceId.SheDevWarsaw2024 -> Sessionize.importSheDevWarsaw2024()
        ConferenceId.AndroidMakers2024 -> Sessionize.importAndroidMakers2024()
        ConferenceId.KotlinConf2024 -> Sessionize.importKotlinConf2024()
        ConferenceId.DroidconBerlin2024 -> Sessionize.importDroidconBerlin2024()
        ConferenceId.DroidconBerlin2025 -> Sessionize.importDroidconBerlin2025()
        ConferenceId.DevFestLille2024 -> DevfestLille.import()
        ConferenceId.DroidConLisbon2024 -> importDroidconLisbon2024()
        ConferenceId.DroidConLondon2024 -> importDroidconLondon2024()
        ConferenceId.DevFestVenice2024 -> importDevFestVenice2024()
        ConferenceId.DevFestWarsaw2024 -> Sessionize.importDevFestWarsaw2024()
        ConferenceId.AndroidMakers2025 -> Sessionize.importAndroidMakers2025()
        ConferenceId.KotlinConf2025 -> Sessionize.importKotlinConf2025()
        ConferenceId.DroidConLondon2025 -> importDroidconLondon2025()
        ConferenceId.DevFestVenice2025 -> importDevFestVenice2025()
        null -> error("")
    }
}

