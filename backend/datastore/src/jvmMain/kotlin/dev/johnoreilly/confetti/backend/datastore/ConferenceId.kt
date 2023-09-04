package dev.johnoreilly.confetti.backend.datastore

enum class ConferenceId(val id: String) {
    DroidConSF2022("droidconsf"),
    DevFestNantes2022("devfestnantes"),
    FrenchKit2022("frenchkit2022"),
    GraphQLSummit2022("graphqlsummit2022"),
    DroidConLondon2022("droidconlondon2022"),
    Fosdem2023("fosdem2023"),
    KotlinConf2023("kotlinconf2023"),
    AndroidMakers2023("androidmakers2023"),
    FlutterConnection2023("flutterconnection2023"),
    ReactNativeConnection2023("reactnativeconnection2023"),
    TestConference("test"),
    DroidconSF2023("droidconsf2023"),
    DroidconBerlin2023("droidconberlin2023"),
    DroidconNYC2023("droidconnyc2023"),
    DevFestNantes2023("devfestnantes2023"),
    ;

    companion object {
        fun from(conf: String?): ConferenceId? {
            return ConferenceId.values().singleOrNull { it.id == conf }
        }
    }
}