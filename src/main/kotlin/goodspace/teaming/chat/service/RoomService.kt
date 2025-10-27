package goodspace.teaming.chat.service

import goodspace.teaming.chat.dto.*

interface RoomService {
    fun createRoom(
        userId: Long,
        requestDto: RoomCreateRequestDto
    ): RoomCreateResponseDto

    fun searchRoom(
        inviteCode: String
    ): RoomSearchResponseDto

    fun acceptInvite(
        userId: Long,
        requestDto: InviteAcceptRequestDto
    ): RoomInfoResponseDto

    fun getInviteCode(
        userId: Long,
        roomId: Long
    ): RoomInviteCodeResponseDto

    fun getRooms(
        userId: Long
    ): List<RoomInfoResponseDto>

    fun updateRoom(
        userId: Long,
        roomId: Long,
        requestDto: RoomUpdateRequestDto
    )

    fun leaveRoom(
        userId: Long,
        roomId: Long
    )

    fun setSuccess(
        userId: Long,
        roomId: Long
    )

    fun isReady(
        userId: Long,
        roomId: Long
    ): RoomReadyResponseDto
}
