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
    DevFestNantes2024("devfestnantes2024"),
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
    DevFestSriLanka2023("devfestsrilanka2023"),
    DroidconAmman2024("droidconamman2024"),
    SheDevWarsaw2024("shedevwarsaw2024"),
    AndroidMakers2024("androidmakers2024"),
    KotlinConf2024("kotlinconf2024"),
    DroidconBerlin2024("droidconberlin2024"),
    DevFestLille2024("devfestlille2024"),
    DroidConLisbon2024("droidconlisbon2024"),
    DroidConLondon2024("droidconlondon2024"),
    DevFestVenice2024("devfestvenice2024"),
    DevFestWarsaw2024("devfestwarsaw2024"),
    AndroidMakers2025("androidmakers2025"),
    KotlinConf2025("kotlinconf2025"),
    ;

    companion object {
        fun from(conf: String?): ConferenceId? {
            return ConferenceId.values().singleOrNull { it.id == conf }
        }
    }
}
