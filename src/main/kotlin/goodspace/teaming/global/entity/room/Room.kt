package goodspace.teaming.global.entity.room

import goodspace.teaming.global.entity.BaseEntity
import jakarta.persistence.*
import jakarta.persistence.CascadeType.*
import jakarta.persistence.EnumType.*
import jakarta.persistence.FetchType.*
import jakarta.persistence.GenerationType.IDENTITY
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.lang.IllegalStateException

@Entity
@Table(name = "`room`")
@SQLDelete(sql = "UPDATE `room` SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class Room(
    @Column(nullable = false)
    var title: String,

    var imageKey: String? = null,

    var imageVersion: Int? = null,

    @Enumerated(STRING)
    @Column(nullable = false)
    val type: RoomType,

    @Column(unique = true)
    var inviteCode: String? = null,

    @Column(nullable = false)
    val memberCount: Int
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null

    @OneToMany(fetch = LAZY, mappedBy = "room", cascade = [ALL], orphanRemoval = true)
    val userRooms: MutableList<UserRoom> = mutableListOf()

    var success: Boolean = false

    fun isEmpty(): Boolean {
        return userRooms.isEmpty()
    }

    fun addUserRoom(userRoom: UserRoom) {
        require(userRooms.size <= memberCount) { throw IllegalStateException("방의 최대 인원 수를 초과했습니다.") }

        userRooms.add(userRoom)
    }

    fun removeUserRoom(userRoom: UserRoom) {
        userRooms.remove(userRoom)
    }
}
