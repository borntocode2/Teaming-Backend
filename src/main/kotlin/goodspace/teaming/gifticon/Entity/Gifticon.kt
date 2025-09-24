package goodspace.teaming.gifticon.Entity

import goodspace.teaming.global.entity.BaseEntity
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
    protected val code: String,
    protected val expirationDate: LocalDateTime
): BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
}