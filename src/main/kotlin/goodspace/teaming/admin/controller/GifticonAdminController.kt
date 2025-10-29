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

    @GetMapping
    @Operation(
        summary = "전체 기프티콘 조회",
        description = "서비스에 존재하는 전체 기프티콘을 조회합니다."
    )
    fun getGifticons(): ResponseEntity<List<GifticonDetailResponseDto>> {
        val response = gifticonService.getGifticonDetails()

        return ResponseEntity.ok(response)
    }

    @DeleteMapping
    @Operation(
        summary = "기프티콘 제거",
        description = "기프티콘을 제거합니다. 사용자에게 이미 전송한 기프티콘은 포함할 수 없습니다."
    )
    fun deleteGifticons(
        @RequestBody requestDto: GifticonDeleteRequestDto
    ): ResponseEntity<Void> {
        gifticonService.removeNotSentGifticons(requestDto)

        return NO_CONTENT
    }
}
