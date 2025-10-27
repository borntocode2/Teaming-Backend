package goodspace.teaming.chat.service

import goodspace.teaming.chat.domain.InviteCodeGenerator
import goodspace.teaming.chat.domain.mapper.*
import goodspace.teaming.chat.dto.*
import goodspace.teaming.chat.event.MemberEnteredEvent
import goodspace.teaming.chat.event.RoomSuccessEvent
import goodspace.teaming.chat.exception.InviteCodeAllocationFailedException
import goodspace.teaming.global.entity.room.PaymentStatus
import goodspace.teaming.global.entity.room.RoomRole
import goodspace.teaming.global.entity.room.RoomType
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.exception.*
import goodspace.teaming.global.repository.RoomRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RoomServiceImpl(
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository,
    private val userRoomRepository: UserRoomRepository,
    private val roomMapper: RoomMapper,
    private val memberMapper: RoomMemberMapper,
    private val roomSearchMapper: RoomSearchMapper,
    private val roomInfoMapper: RoomInfoMapper,
    private val roomReadyMapper: RoomReadyMapper,
    private val inviteCodeGenerator: InviteCodeGenerator,
    private val eventPublisher: ApplicationEventPublisher
) : RoomService {
    @Transactional
    @Retryable(
        include = [DataIntegrityViolationException::class, InviteCodeAllocationFailedException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 50, multiplier = 2.0)
    )
    override fun createRoom(
        userId: Long,
        requestDto: RoomCreateRequestDto
    ): RoomCreateResponseDto {
        val user = userRepository.findById(userId).orElse(null)
            ?: throw IllegalArgumentException(USER_NOT_FOUND)

        val room = roomMapper.map(requestDto)
        room.inviteCode = getUniqueInviteCode()

        val userRoom = UserRoom(
            user = user,
            room = room,
            roomRole = RoomRole.LEADER
        )
        user.addUserRoom(userRoom)
        room.addUserRoom(userRoom)

        passPaymentIfDemoRoom(userRoom)

        // 초대 코드가 중복될 시 재시도하기 위한 플러쉬
        roomRepository.saveAndFlush(room)

        return RoomCreateResponseDto(
            roomId = room.id!!,
            inviteCode = room.inviteCode!!
        )
    }

    @Transactional(readOnly = true)
    override fun searchRoom(inviteCode: String): RoomSearchResponseDto {
        val room = roomRepository.findByInviteCode(inviteCode)
            ?: throw IllegalArgumentException(WRONG_INVITE_CODE)

        return roomSearchMapper.map(room)
    }

    @Transactional
    override fun acceptInvite(userId: Long, requestDto: InviteAcceptRequestDto): RoomInfoResponseDto {
        val user = userRepository.findById(userId).orElse(null)
            ?: throw IllegalArgumentException(USER_NOT_FOUND)
        val room = roomRepository.findByInviteCode(requestDto.inviteCode)
            ?: throw IllegalArgumentException(WRONG_INVITE_CODE)

        require(!userRoomRepository.existsByRoomAndUser(room, user)) { ALREADY_MEMBER_OF_ROOM }

        val userRoom = UserRoom(
            user = user,
            room = room,
            roomRole = RoomRole.MEMBER,
        )
        user.addUserRoom(userRoom)
        room.addUserRoom(userRoom)

        passPaymentIfDemoRoom(userRoom)

        eventPublisher.publishEvent(MemberEnteredEvent(room.id!!, memberMapper.map(userRoom)))

        return roomInfoMapper.map(userRoom)
    }

    @Transactional(readOnly = true)
    override fun getInviteCode(userId: Long, roomId: Long): RoomInviteCodeResponseDto {
        val userRoom = findUserRoom(userId, roomId)

        assertLeader(userRoom)

        val room = userRoom.room

        return RoomInviteCodeResponseDto(inviteCode = room.inviteCode!!)
    }

    @Transactional(readOnly = true)
    override fun getRooms(userId: Long): List<RoomInfoResponseDto> {
        val user = userRepository.findById(userId).orElse(null)
            ?: throw IllegalArgumentException(USER_NOT_FOUND)

        return user.userRooms
            .map { roomInfoMapper.map(it) }
    }

    @Transactional
    override fun updateRoom(
        userId: Long,
        roomId: Long,
        requestDto: RoomUpdateRequestDto
    ) {
        requestDto.validate()

        val userRoom = findUserRoom(userId, roomId)
        val room = userRoom.room

        assertLeader(userRoom)

        room.title = requestDto.title
        room.description = requestDto.description
    }

    @Transactional
    override fun leaveRoom(userId: Long, roomId: Long) {
        val userRoom = findUserRoom(userId, roomId)
        val room = userRoom.room

        check(room.success) { CANNOT_LEAVE_BEFORE_SUCCESS }

        room.removeUserRoom(userRoom)

        if (room.isEmpty()) {
            roomRepository.delete(room)
        }
    }

    @Transactional
    override fun setSuccess(userId: Long, roomId: Long) {
        val userRoom = findUserRoom(userId, roomId)
        val room = userRoom.room

        check(userRoom.roomRole == RoomRole.LEADER) { NOT_LEADER }

        eventPublisher.publishEvent(RoomSuccessEvent(roomId = room.id!!))

        room.success = true
    }

    @Transactional(readOnly = true)
    override fun isReady(
        userId: Long,
        roomId: Long
    ): RoomReadyResponseDto {
        val userRoom = findUserRoom(userId, roomId)

        return roomReadyMapper.map(userRoom.room)
    }

    private fun findUserRoom(userId: Long, roomId: Long): UserRoom {
        return userRoomRepository.findByRoomIdAndUserId(roomId, userId)
            ?: throw IllegalArgumentException(ROOM_NOT_FOUND)
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

    private fun passPaymentIfDemoRoom(userRoom: UserRoom) {
        val room = userRoom.room

        if (room.type == RoomType.DEMO) {
            userRoom.paymentStatus = PaymentStatus.PAID
        }
    }

    private fun assertLeader(userRoom: UserRoom) {
        check(userRoom.roomRole == RoomRole.LEADER) { NOT_LEADER }
    }

    private fun RoomUpdateRequestDto.validate() {
        require(title.trim().isNotEmpty()) { ILLEGAL_ROOM_TITLE }
    }
}
