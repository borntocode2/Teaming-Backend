
import goodspace.teaming.gifticon.dto.GifticonRequestDto
import goodspace.teaming.gifticon.dto.GifticonResponseDto
import goodspace.teaming.gifticon.service.GifticonService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/gifticon")
@RestController
@Tag(
    name = "관리자 API"
)
class GifticonController(
    private val gifticonService: GifticonService) {

    @GetMapping
    @Operation(
        summary = "기프티콘 조회",
        description = "유저 이메일로 해당 유저의 기프티콘을 조회합니다."
    )
    fun getGifticon(@RequestParam email: String): ResponseEntity<List<GifticonResponseDto>>{
        val dtos: List<GifticonResponseDto> = gifticonService.getGifticonsByUserEmail(email)


        return ResponseEntity.ok(dtos)
    }
}