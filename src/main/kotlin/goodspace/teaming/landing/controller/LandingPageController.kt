package goodspace.teaming.landing.controller

import goodspace.teaming.landing.dto.StatisticResponseDto
import goodspace.teaming.landing.service.LandingPageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/landing")
@Tag(
    name = "랜딩 페이지 API",
    description = "랜딩 페이지용 API입니다. JWT를 필요로 하지 않습니다."
)
class LandingPageController(
    private val landingPageService: LandingPageService
) {
    @GetMapping
    @Operation(
        summary = "통계 조회",
        description = "현재 만들어진 팀 개수, 현재 가입된 이용자 개수, 프로젝트 완수한 팀 개수를 반환합니다.",
        security = []
    )
    fun getStatistics(): ResponseEntity<StatisticResponseDto> {
        val response = landingPageService.getStatistics()

        return ResponseEntity.ok(response)
    }
}
