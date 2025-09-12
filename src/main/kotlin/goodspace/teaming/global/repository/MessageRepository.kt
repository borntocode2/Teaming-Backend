package goodspace.teaming.global.repository

import goodspace.teaming.global.entity.room.Message
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.user.User
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface MessageRepository : JpaRepository<Message, Long> {
    fun findByClientMessageIdAndRoomAndSender(clientMessageId: String, room: Room, sender: User): Message?

    fun findByRoomOrderByIdDesc(room: Room, pageable: Pageable): Slice<Message>

    fun findByRoomAndIdLessThanOrderByIdDesc(
        room: Room,
        id: Long,
        pageable: Pageable
    ): Slice<Message>

    @Query("select max(m.id) from Message m where m.room = :room")
    fun findLatestMessageId(@Param("room") room: Room): Long?

    @Query("""
        select count(m)
        from Message m
        where m.room = :room
          and (:lastId is null or m.id > :lastId)
          and m.sender <> :user
    """)
    fun countUnreadInRoom(
        @Param("room") room: Room,
        @Param("user") user: User,
        @Param("lastId") lastReadMessageId: Long?
    ): Long
}
