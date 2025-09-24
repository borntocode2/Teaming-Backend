package goodspace.teaming.gifticon.Entity

import goodspace.teaming.global.entity.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
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