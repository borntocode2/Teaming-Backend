package goodspace.teaming.gifticon.Entity

import java.time.LocalDateTime


abstract class Gifticon (
    protected val code: String,
    protected val expirationDate: LocalDateTime
)