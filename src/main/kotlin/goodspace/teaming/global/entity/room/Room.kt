package goodspace.teaming.global.entity.room

import goodspace.teaming.global.entity.BaseEntity
import jakarta.persistence.*
import jakarta.persistence.EnumType.*
import jakarta.persistence.GenerationType.IDENTITY
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction

@Entity
@Table(name = "`room`")
@SQLDelete(sql = "UPDATE `room` SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
class Room(
    @Column(nullable = false)
    var title: String,

    var image: ByteArray? = null,

    @Enumerated(STRING)
    @Column(nullable = false)
    val type: RoomType,

    @Column(nullable = false, unique = true)
    var inviteCode: String
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null
}
