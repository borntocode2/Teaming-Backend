package goodspace.teaming.global.entity.room

import goodspace.teaming.global.entity.BaseEntity
import goodspace.teaming.global.entity.user.User
import jakarta.persistence.*
import jakarta.persistence.EnumType.*
import jakarta.persistence.GenerationType.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@SQLDelete(sql = "UPDATE user_room SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class UserRoom(
    @ManyToOne
    @JoinColumn(nullable = false)
    val user: User,

    @ManyToOne
    @JoinColumn(nullable = false)
    val room: Room,

    @Enumerated(STRING)
    @Column(nullable = false)
    val roomRole: RoomRole,

    val lastReadMessageId: Long? = null,

    @Column(nullable = false)
    val paid: Boolean = false
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null
}
