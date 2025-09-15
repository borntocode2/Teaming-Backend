package goodspace.teaming.chat.service

import goodspace.teaming.chat.domain.InviteCodeGenerator
import goodspace.teaming.chat.domain.mapper.RoomInfoMapper
import goodspace.teaming.chat.domain.mapper.RoomMapper
import goodspace.teaming.chat.dto.InviteAcceptRequestDto
import goodspace.teaming.chat.dto.RoomCreateRequestDto
import goodspace.teaming.chat.dto.RoomInfoResponseDto
import goodspace.teaming.chat.exception.InviteCodeAllocationFailedException
import goodspace.teaming.global.entity.room.PaymentStatus
import goodspace.teaming.global.entity.room.RoomRole
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.repository.RoomRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val USER_NOT_FOUND = "회원을 조회할 수 없습니다."
private const val ROOM_NOT_FOUND = "티밍룸을 조회할 수 없습니다."
private const val ALREADY_JOINED = "이미 해방 티밍룸에 소속되어 있습니다."
private const val WRONG_INVITE_CODE = "부적절한 초대 코드입니다."
private const val NOT_PAID = "결제되지 않았습니다."
private const val NOT_LEADER = "팀장이 아닙니다."

@Service
class RoomServiceImpl(
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository,
    private val userRoomRepository: UserRoomRepository,
    private val roomMapper: RoomMapper,
    private val roomInfoMapper: RoomInfoMapper,
    private val inviteCodeGenerator: InviteCodeGenerator
) : RoomService {
    @Transactional
    @Retryable(
        include = [DataIntegrityViolationException::class, InviteCodeAllocationFailedException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 50, multiplier = 2.0)
    )
    override fun createRoom(userId: Long, requestDto: RoomCreateRequestDto) {
        val user = userRepository.findById(userId).orElse(null)
            ?: throw IllegalArgumentException(USER_NOT_FOUND)

        val room = roomMapper.map(requestDto)
        room.inviteCode = getUniqueInviteCode()

        val userRoom = UserRoom(
            user = user,
            room = room,
            roomRole = RoomRole.LEADER
        )
        room.addUserRoom(userRoom)

        // 초대 코드가 중복될 시 재시도하기 위한 플러쉬
        roomRepository.saveAndFlush(room)
    }

    @Transactional
    override fun acceptInvite(userId: Long, requestDto: InviteAcceptRequestDto): RoomInfoResponseDto {
        val user = userRepository.findById(userId).orElse(null)
            ?: throw IllegalArgumentException(USER_NOT_FOUND)
        val room = roomRepository.findByInviteCode(requestDto.inviteCode)
            ?: throw IllegalArgumentException(WRONG_INVITE_CODE)

        require(!userRoomRepository.existsByRoomAndUser(room, user)) { ALREADY_JOINED }

        val userRoom = UserRoom(
            user = user,
            room = room,
            roomRole = RoomRole.MEMBER,
        )
        user.addUserRoom(userRoom)
        room.addUserRoom(userRoom)

        return roomInfoMapper.map(userRoom)
    }

    @Transactional(readOnly = true)
    override fun getRooms(userId: Long): List<RoomInfoResponseDto> {
        val user = userRepository.findById(userId).orElse(null)
            ?: throw IllegalArgumentException(USER_NOT_FOUND)

        return user.userRooms
            .map { roomInfoMapper.map(it) }
    }

    @Transactional
    override fun leaveRoom(userId: Long, roomId: Long) {
        val userRoom = userRoomRepository.findByRoomIdAndUserId(roomId, userId)
            ?: throw IllegalArgumentException(ROOM_NOT_FOUND)
        val room = userRoom.room

        require(userRoom.paymentStatus != PaymentStatus.NOT_PAID) { NOT_PAID }

        if (userRoom.paymentStatus == PaymentStatus.PAID) {
            // TODO: 벌칙 이벤트 발생
        }

        room.removeUserRoom(userRoom)

        if (room.isEmpty()) {
            roomRepository.delete(room)
        }
    }

    @Transactional
    override fun setSuccess(userId: Long, roomId: Long) {
        val userRoom = userRoomRepository.findByRoomIdAndUserId(roomId, userId)
            ?: throw IllegalArgumentException(ROOM_NOT_FOUND)
        val room = userRoom.room

        check(userRoom.roomRole == RoomRole.LEADER) { NOT_LEADER }

        // TODO 환불 이벤트 발생

        room.success = true
    }

    private fun getUniqueInviteCode(): String {
        var tryCount = 0

        while (tryCount < 10) {
            val inviteCode = inviteCodeGenerator.generate()

            if (!roomRepository.existsByInviteCode(inviteCode)) {
                return inviteCode
            }
            tryCount++
        }

        throw InviteCodeAllocationFailedException()
    }
}
