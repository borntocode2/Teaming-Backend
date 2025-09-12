package goodspace.teaming.chat.dto

data class ChatMessagePageResponseDto(
    val items: List<ChatMessageResponseDto>,
    val hasNext: Boolean,
    val nextCursor: Long?
)
