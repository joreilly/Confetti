package fr.androidmakers.server.model

@kotlinx.serialization.Serializable
data class JsonAgenda(
    val talks: Map<String, List<JsonTalkWrapper>>
)

@kotlinx.serialization.Serializable
data class JsonTalkWrapper(
    val id: String,
    val time: String,
    val startTime: String,
    val endTime: String,
    val roomId: String = "unknown",
    val talk: JsonTalk? = null
)

@kotlinx.serialization.Serializable
data class JsonTalk(
    val id: String,
    val title: String,
    val level: String? = null,
    val abstract: String,
    val category: String,
    val format: String,
    val language: String,
    val speakers: List<JsonSpeakers>,
)

@kotlinx.serialization.Serializable
data class JsonSpeakers(
    val id: String,
    val display_name: String,
    val bio: String,
    val company: String,
    val photo_url: String,
    val twitter: String?,
    val github:String?,
)