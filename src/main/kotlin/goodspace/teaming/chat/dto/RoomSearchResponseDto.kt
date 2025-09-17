package goodspace.teaming.chat.dto

data class RoomSearchResponseDto(
    val title: String,
    val imageKey: String?,
    val imageVersion: Int?,
    val type: RoomTypeResponseDto,
    val currentMemberCount: Int,
    val maxMemberCount: Int,
)
