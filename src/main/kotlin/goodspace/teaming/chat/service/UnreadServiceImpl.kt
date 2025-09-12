package goodspace.teaming.chat.service

import goodspace.teaming.chat.domain.mapper.RoomUnreadCountMapper
import goodspace.teaming.chat.dto.RoomUnreadCountResponseDto
import goodspace.teaming.global.repository.MessageRepository
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalArgumentException

private const val ROOM_NOT_FOUND = "티밍룸을 조회할 수 없습니다."

@Service
class UnreadServiceImpl(
    private val userRoomRepository: UserRoomRepository,
    private val messageRepository: MessageRepository,
    private val roomUnreadCountMapper: RoomUnreadCountMapper
) : UnreadService {
    @Transactional(readOnly = true)
    override fun getUnreadCounts(userId: Long): List<RoomUnreadCountResponseDto> {
        val userRooms = userRoomRepository.findByUserId(userId)

        return userRooms.map { roomUnreadCountMapper.map(it) }
    }

    /**
     * lastReadMessageId가 null일 경우, 최신 메시지까지 읽은 것으로 판단한다
     */
    @Transactional
    override fun markRead(userId: Long, roomId: Long, lastReadMessageId: Long?): RoomUnreadCountResponseDto {
        val userRoom = (userRoomRepository.findByRoomIdAndUserId(roomId, userId)
            ?: throw IllegalArgumentException(ROOM_NOT_FOUND))

        val latestMessageId = messageRepository.findLatestMessageId(userRoom.room)
            // 메시지가 하나도 없는 방이라면 DTO를 즉시 반환한다
            ?: return roomUnreadCountMapper.map(userRoom)
        val currentReadId = userRoom.lastReadMessageId

        val clampedLastReadMessageId = clampRequestedLastReadId(lastReadMessageId, latestMessageId)
        val newLastReadId = nextMonotonicLastReadId(currentReadId, clampedLastReadMessageId)

        if (shouldRaise(newLastReadId, currentReadId)) {
            userRoomRepository.raiseLastReadMessageId(userId, roomId, newLastReadId!!)
        }

        val updatedUserRoom = userRoomRepository.findByRoomIdAndUserId(roomId, userId)!!

        return roomUnreadCountMapper.map(updatedUserRoom)
    }

    /**
     * 요청 값의 lastReadMessageId를 보정한다
     * - requestedId가 null일 경우, 최신 메시지까지 읽은 것으로 판단해 보정한다
     * - 가장 최신 메시지의 id보다 높을 수 없게 보정한다(메시지 삭제로 인한 불일치 대비)
     */
    private fun clampRequestedLastReadId(requestedId: Long?, latestId: Long): Long {
        // null일 경우 최신 메시지까지 읽은 것으로 판단
        requestedId ?: return latestId

        // 최신 메시지가 삭제됐을 때를 대비한 방어 로직
        return kotlin.math.min(requestedId, latestId)
    }

    /**
     * 동시성 문제로 인해 lastReadMessage가 낮아지지 않도록 더 높은 값을 반환한다
     */
    private fun nextMonotonicLastReadId(currentReadId: Long?, candidateReadId: Long?): Long? {
        return when {
            candidateReadId == null -> currentReadId
            currentReadId == null -> candidateReadId
            else -> kotlin.math.max(currentReadId, candidateReadId)
        }
    }

    private fun shouldRaise(newLastReadId: Long?, currentReadId: Long?): Boolean {
        return newLastReadId != null && newLastReadId != currentReadId
    }
}
