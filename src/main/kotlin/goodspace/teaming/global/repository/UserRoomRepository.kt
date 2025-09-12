package goodspace.teaming.global.repository

import goodspace.teaming.global.entity.room.UserRoom
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRoomRepository : JpaRepository<UserRoom, Long> {
    fun findByRoomIdAndUserId(roomId: Long, userId: Long): UserRoom?

    fun existsByRoomIdAndUserId(roomId: Long, userId: Long): Boolean

    fun findByUserId(userId: Long): List<UserRoom>

    fun findByRoomId(roomId: Long): List<UserRoom>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update UserRoom ur
           set ur.lastReadMessageId = :newLastReadMessageId
         where ur.user.id = :userId
           and ur.room.id = :roomId
           and (ur.lastReadMessageId is null or ur.lastReadMessageId < :newLastReadMessageId)
    """)
    fun raiseLastReadMessageId(
        @Param("userId") userId: Long,
        @Param("roomId") roomId: Long,
        @Param("newLastReadMessageId") newLastReadMessageId: Long
    ): Int
}
