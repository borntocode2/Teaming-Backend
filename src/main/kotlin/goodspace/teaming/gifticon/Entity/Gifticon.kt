package goodspace.teaming.gifticon.Entity

import goodspace.teaming.global.entity.BaseEntity
import goodspace.teaming.global.entity.user.User
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@SQLDelete(sql = "UPDATE `assigned_member` SET deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted = false")
@Entity
@DiscriminatorColumn(name = "gifticon_type")
abstract class Gifticon (
    var code: String,
    var expirationDate: LocalDateTime,

    @Column(nullable = false)
    var grade: Grade,

    @Column(nullable = false)
    var isSent: Boolean = false,

    @Column(nullable = false)
var used: Boolean = false   // ← 여기 추가
): BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null
}