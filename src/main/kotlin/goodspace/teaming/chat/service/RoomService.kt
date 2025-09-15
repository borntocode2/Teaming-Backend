package goodspace.teaming.chat.service

import goodspace.teaming.chat.dto.InviteAcceptRequestDto
import goodspace.teaming.chat.dto.RoomCreateRequestDto
import goodspace.teaming.chat.dto.RoomInfoResponseDto

interface RoomService {
    fun createRoom(
        userId: Long,
        requestDto: RoomCreateRequestDto
    )

    fun acceptInvite(
        userId: Long,
        requestDto: InviteAcceptRequestDto
    ): RoomInfoResponseDto

    fun getRooms(
        userId: Long
    ): List<RoomInfoResponseDto>

    fun leaveRoom(
        userId: Long,
        roomId: Long
    )

    fun setSuccess(
        userId: Long,
        roomId: Long
    )
}
