package goodspace.teaming.gifticon.Entity

import java.time.LocalDateTime

class StandardGifticon (
    code: String,
    expirationDate: LocalDateTime,
    grade: Grade
): Gifticon(code, expirationDate)