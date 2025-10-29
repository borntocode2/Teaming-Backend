package goodspace.teaming.chat.dto

data class RoomReadyResponseDto(
    val everyMemberEntered: Boolean,
    val everyMemberPaid: Boolean
)
