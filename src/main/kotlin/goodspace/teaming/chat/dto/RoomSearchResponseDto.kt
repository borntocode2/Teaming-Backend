package goodspace.teaming.chat.dto

data class RoomSearchResponseDto(
    val title: String,
    val avatarUrl: String?,
    val avatarVersion: Int?,
    val type: RoomTypeResponseDto,
    val currentMemberCount: Int,
    val maxMemberCount: Int,
)
