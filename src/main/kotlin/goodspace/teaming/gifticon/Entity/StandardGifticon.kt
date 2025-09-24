package goodspace.teaming.gifticon.Entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class StandardGifticon (
    code: String,
    expirationDate: LocalDateTime,
    grade: Grade
): Gifticon(code, expirationDate){
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long ?= null
}