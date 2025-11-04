package goodspace.teaming.gifticon.domain

import goodspace.teaming.gifticon.Entity.Gifticon
import goodspace.teaming.gifticon.dto.GifticonDetailResponseDto
import org.springframework.stereotype.Component

@Component
class GifticonDetailMapper {
    fun map(gifticon: Gifticon): GifticonDetailResponseDto {
        return GifticonDetailResponseDto(
            id = gifticon.id!!,
            code = gifticon.code,
            expirationDate = gifticon.expirationDate,
            grade = gifticon.grade,
            isSent = gifticon.isSent,
            isUsed = gifticon.used
        )
    }
}
