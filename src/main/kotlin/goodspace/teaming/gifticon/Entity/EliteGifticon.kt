package goodspace.teaming.gifticon.Entity

import java.time.LocalDateTime

class EliteGifticon (
    code: String,
    expirationDate: LocalDateTime,
    grade: Grade
): Gifticon(code, expirationDate)