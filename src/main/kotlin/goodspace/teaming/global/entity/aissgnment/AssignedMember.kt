package goodspace.teaming.global.entity.aissgnment

import goodspace.teaming.global.entity.BaseEntity
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.entity.user.User
import jakarta.persistence.*
import jakarta.persistence.FetchType.*
import jakarta.persistence.GenerationType.IDENTITY
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SQLDelete(sql = "UPDATE `assigned_member` SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class AssignedMember(
    @ManyToOne(fetch = LAZY)
    @JoinColumn(nullable = false)
    val userRoom: UserRoom,

    @ManyToOne(fetch = LAZY)
    @JoinColumn(nullable = false)
    val assignment: Assignment,
) : BaseEntity() {
    val user: User
        get() = userRoom.user

    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null
}
