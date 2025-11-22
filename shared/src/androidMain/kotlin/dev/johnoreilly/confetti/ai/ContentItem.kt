package dev.johnoreilly.confetti.ai

import android.net.Uri

/**
 * Represents a generic content item that can be rendered in a RecyclerView and holds GenAI API
 * request or response data.
 */
sealed interface ContentItem {

  val viewType: Int

  /** A content item that contains only text. */
  data class TextItem(val text: String, val metadata: String? = null, override val viewType: Int) :
    ContentItem {
    companion object {
      fun fromRequest(request: String): TextItem = TextItem(request, null, VIEW_TYPE_REQUEST_TEXT)

      fun fromResponse(response: String, metadata: String?): TextItem =
        TextItem(response, metadata, VIEW_TYPE_RESPONSE)

      fun fromErrorResponse(response: String): TextItem =
        TextItem(response, null, VIEW_TYPE_RESPONSE_ERROR)

      fun fromStreamingResponse(response: String): TextItem =
        TextItem(response, null, VIEW_TYPE_RESPONSE_STREAMING)
    }
  }

  /** A content item that contains only one image. */
  data class ImageItem(val imageUri: Uri, override val viewType: Int) : ContentItem {
    companion object {
      fun fromRequest(imageUri: Uri): ImageItem = ImageItem(imageUri, VIEW_TYPE_REQUEST_IMAGE)
    }
  }

  /** A content item that contains both text and one or more images. */
  data class TextAndImagesItem(
    val text: String,
    val imageUris: List<Uri>,
    override val viewType: Int,
  ) : ContentItem {
    companion object {
      fun fromRequest(text: String, imageUris: List<Uri>): TextAndImagesItem =
        TextAndImagesItem(text, imageUris, VIEW_TYPE_REQUEST_TEXT_AND_IMAGES)
    }
  }

  /** A content item that contains a prompt prefix and a dynamic suffix. */
  data class TextWithPromptPrefixItem(
    val promptPrefix: String,
    val dynamicSuffix: String,
    override val viewType: Int,
  ) : ContentItem {
    companion object {
      fun fromRequest(promptPrefix: String, dynamicSuffix: String): TextWithPromptPrefixItem =
        TextWithPromptPrefixItem(
          promptPrefix,
          dynamicSuffix,
          VIEW_TYPE_REQUEST_TEXT_WITH_PROMPT_PREFIX,
        )
    }
  }


  companion object {
    const val VIEW_TYPE_REQUEST_TEXT: Int = 0
    const val VIEW_TYPE_REQUEST_IMAGE: Int = 1
    const val VIEW_TYPE_RESPONSE: Int = 2
    const val VIEW_TYPE_RESPONSE_STREAMING: Int = 3
    const val VIEW_TYPE_RESPONSE_ERROR: Int = 4
    const val VIEW_TYPE_REQUEST_TEXT_AND_IMAGES: Int = 5
    const val VIEW_TYPE_REQUEST_TEXT_WITH_PROMPT_PREFIX: Int = 6
  }
}
