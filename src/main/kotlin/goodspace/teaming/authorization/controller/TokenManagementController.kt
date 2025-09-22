package goodspace.teaming.authorization.controller

import goodspace.teaming.authorization.dto.AccessTokenReissueRequestDto
import goodspace.teaming.authorization.dto.AccessTokenResponseDto
import goodspace.teaming.authorization.service.TokenManagementService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth/token")
@Tag(
    name = "JWT 관리 API"
)
class TokenManagementController(
    private val tokenManagementService: TokenManagementService
) {
    @PostMapping("/access-token")
    @Operation(
        summary = "엑세스 토큰 재발급",
        description = "리프레쉬 토큰을 통해 엑세스 토큰을 재발급합니다."
    )
    fun reissueAccessToken(
        @RequestBody requestDto: AccessTokenReissueRequestDto
    ): ResponseEntity<AccessTokenResponseDto> {
        val response = tokenManagementService.reissueAccessToken(requestDto)

        return ResponseEntity.ok(response)
    }
}
