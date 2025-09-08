package goodspace.teaming.global.repository

import goodspace.teaming.global.entity.room.UserRoom
import org.springframework.data.jpa.repository.JpaRepository

interface UserRoomRepository : JpaRepository<UserRoom, Long> {
    fun findByRoomIdAndUserId(roomId: Long, userId: Long): UserRoom?

    fun existsByRoomIdAndUserId(roomId: Long, userId: Long): Boolean
}
