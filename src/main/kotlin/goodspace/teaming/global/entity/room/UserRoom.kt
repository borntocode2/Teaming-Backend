package goodspace.teaming.global.entity.room

import goodspace.teaming.global.entity.BaseEntity
import goodspace.teaming.global.entity.aissgnment.AssignedMember
import goodspace.teaming.global.entity.user.User
import jakarta.persistence.*
import jakarta.persistence.EnumType.*
import jakarta.persistence.FetchType.*
import jakarta.persistence.GenerationType.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@SQLDelete(sql = "UPDATE user_room SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class UserRoom(
    @ManyToOne(fetch = LAZY)
    @JoinColumn(nullable = false)
    val user: User,

    @ManyToOne(fetch = LAZY)
    @JoinColumn(nullable = false)
    val room: Room,

    @Enumerated(STRING)
    @Column(nullable = false)
    val roomRole: RoomRole,

    var lastReadMessageId: Long? = null,

    @Column(nullable = false)
    var paymentStatus: PaymentStatus = PaymentStatus.NOT_PAID
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null

    @OneToMany(mappedBy = "userRoom", cascade = [CascadeType.ALL], orphanRemoval = true)
    val assignedMembers: MutableList<AssignedMember> = mutableListOf()
}
