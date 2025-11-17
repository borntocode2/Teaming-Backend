package goodspace.teaming.push.controller

import goodspace.teaming.global.security.userId
import goodspace.teaming.push.dto.PushTokenDeleteRequestDto
import goodspace.teaming.push.dto.PushTokenRegisterRequestDto
import goodspace.teaming.push.service.PushTokenService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController("/push")
@Tag(
    name = "알림 API"
)
class PushController(
    private val pushTokenService: PushTokenService
) {
    @PostMapping("/tokens")
    @Operation(
        summary = "푸쉬 토큰 등록/갱신",
        description = "사용자의 현재 기기에 대한 푸쉬 토큰을 등록합니다. 이미 등록되어 있을 경우 토큰 등록 일자를 최신화합니다."
    )
    fun registerToken(
        principal: Principal,
        @RequestBody requestDto: PushTokenRegisterRequestDto
    ): ResponseEntity<Void> {
        val userId = principal.userId

        pushTokenService.registerOrUpdate(requestDto, userId)

        return NO_CONTENT
    }

    @DeleteMapping("/tokens")
    @Operation(
        summary = "푸쉬 토큰 삭제",
        description = "사용자의 현재 기기에 대한 푸쉬 토큰을 제거합니다. 로그아웃 시 항상 호출해야 합니다. JWT 만료와 무관하게 호출할 수 있습니다."
    )
    fun deleteToken(
        @RequestBody requestDto: PushTokenDeleteRequestDto
    ): ResponseEntity<Void> {
        pushTokenService.delete(requestDto)

        return NO_CONTENT
    }

    companion object {
        private val NO_CONTENT = ResponseEntity.noContent().build<Void>()
    }
}
