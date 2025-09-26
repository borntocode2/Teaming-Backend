package goodspace.teaming.chat.dto

import goodspace.teaming.global.entity.room.RoomType

data class RoomCreateRequestDto(
    val title: String,
    val description: String,
    val memberCount: Int,
    val roomType: RoomType,
    val avatarKey: String?,
    val avatarVersion: Int = 0
)
