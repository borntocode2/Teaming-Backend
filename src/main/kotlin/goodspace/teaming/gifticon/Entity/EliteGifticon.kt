package goodspace.teaming.gifticon.Entity

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.time.LocalDateTime

@Entity
@DiscriminatorValue("ELITE")
class EliteGifticon (
    code: String,
    expirationDate: LocalDateTime,
    grade: Grade
): Gifticon(code, expirationDate, grade)