package goodspace.teaming.chat.event

import goodspace.teaming.chat.dto.RoomMemberResponseDto

data class MemberEnteredEvent(
    val roomId: Long,
    val member: RoomMemberResponseDto
)
