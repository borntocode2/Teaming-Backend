package goodspace.teaming.gifticon.dto

import goodspace.teaming.gifticon.Entity.Grade

class GifticonResponseDto (
    val code: String,
    val expirationDateStr: String,
    val grade: Grade
    )