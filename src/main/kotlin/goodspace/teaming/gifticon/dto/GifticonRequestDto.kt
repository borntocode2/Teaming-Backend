package goodspace.teaming.gifticon.dto

import goodspace.teaming.gifticon.Entity.Grade

class GifticonRequestDto (
    val code: String,
    val expirationDateStr: String,
    val grade: Grade
)