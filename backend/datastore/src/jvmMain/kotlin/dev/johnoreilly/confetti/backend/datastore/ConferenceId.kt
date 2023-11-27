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
    SwiftConnection2023("swiftconnection2023"),
    DroidConLisbon2023("droidconlisbon2023"),
    DevFestGeorgia2023("devfestgeorgia2023"),
    DevFestMelbourne2023("devfestmelbourne2023"),
    GraphQLSummit2023("graphqlsummit2023"),
    DroidconLondon2023("droidconlondon2023"),
    DevFestWarsaw2023("devfestwarsaw2023"),
    DevFestIreland2023("devfestireland2023"),
    DevFestVenice2023("devfestvenice2023"),
    DevFestStockholm2023("devfeststockholm2023"),
    ;

    companion object {
        fun from(conf: String?): ConferenceId? {
            return ConferenceId.values().singleOrNull { it.id == conf }
        }
    }
}
