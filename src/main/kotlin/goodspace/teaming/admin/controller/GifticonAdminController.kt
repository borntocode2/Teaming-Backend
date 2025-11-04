package goodspace.teaming.admin.controller

import goodspace.teaming.gifticon.dto.GifticonDeleteRequestDto
import goodspace.teaming.gifticon.dto.GifticonDetailResponseDto
import goodspace.teaming.gifticon.dto.GifticonRequestDto
import goodspace.teaming.gifticon.service.GifticonService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val NO_CONTENT: ResponseEntity<Void> = ResponseEntity.noContent().build()

@RequestMapping("/admin/gifticon")
@RestController
@Tag(
    name = "관리자 API"
)
class GifticonAdminController(
    private val gifticonService: GifticonService
) {
    @PostMapping
    @Operation(
        summary = "기프티콘 저장",
        description = "기프티콘 코드, 기프티콘 만료기한 ex)\"20250925\", 기프티콘 등급(\"BASIC\"\",STANDARD\",\"ELITE\")"
    )
    fun saveGifticon(
        @RequestBody gifticonRequestDto: GifticonRequestDto
    ): ResponseEntity<Void> {
        gifticonService.saveGifticon(
            code = gifticonRequestDto.code,
            expiration = gifticonRequestDto.expirationDateStr,
            grade = gifticonRequestDto.grade
        )

        return NO_CONTENT
    }
}
