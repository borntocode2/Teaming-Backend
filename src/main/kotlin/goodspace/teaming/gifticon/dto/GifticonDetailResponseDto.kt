package goodspace.teaming.gifticon.dto

import goodspace.teaming.gifticon.Entity.Grade
import java.time.LocalDateTime

data class GifticonDetailResponseDto(
    val id: Long,
    val code: String,
    val expirationDate: LocalDateTime,
    val grade: Grade,
    val isSent: Boolean,
    val isUsed: Boolean
)
