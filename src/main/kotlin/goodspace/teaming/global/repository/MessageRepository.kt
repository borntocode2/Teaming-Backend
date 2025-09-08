package goodspace.teaming.global.repository

import goodspace.teaming.global.entity.room.Message
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository : JpaRepository<Message, Long> {
    fun findByClientMessageIdAndRoomAndSender(clientMessageId: String, room: Room, sender: User): Message?

    fun findByRoomOrderByCreatedAtDesc(room: Room, pageable: Pageable): List<Message>
}
