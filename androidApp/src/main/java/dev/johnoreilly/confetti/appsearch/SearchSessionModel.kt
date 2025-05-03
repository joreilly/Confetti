package dev.johnoreilly.confetti.appsearch

import androidx.appsearch.annotation.Document
import androidx.appsearch.app.AppSearchSchema

@Document(name = "Session")
data class SearchSessionModel(
    @Document.Namespace
    val conference: String,

    @Document.Id
    val id: String,

    @Document.StringProperty(indexingType = AppSearchSchema.StringPropertyConfig.INDEXING_TYPE_PREFIXES)
    val title: String,

    @Document.StringProperty
    val room: String,

    @Document.StringProperty
    val speakers: List<String>
)