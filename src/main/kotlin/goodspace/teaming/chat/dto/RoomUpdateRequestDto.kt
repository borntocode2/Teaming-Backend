package goodspace.teaming.chat.dto

data class RoomUpdateRequestDto(
    val title: String,
    val description: String? = null
)
