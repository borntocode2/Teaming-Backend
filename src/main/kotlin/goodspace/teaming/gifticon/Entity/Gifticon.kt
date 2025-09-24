package goodspace.teaming.gifticon.Entity

import goodspace.teaming.global.entity.BaseEntity
import jakarta.persistence.MappedSuperclass
import java.time.LocalDateTime

@MappedSuperclass
abstract class Gifticon (
    protected val code: String,
    protected val expirationDate: LocalDateTime
): BaseEntity()