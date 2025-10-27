package goodspace.teaming.user.controller

import goodspace.teaming.authorization.dto.AccessTokenReissueRequestDto
import goodspace.teaming.authorization.dto.AccessTokenResponseDto
import goodspace.teaming.global.security.userId
import goodspace.teaming.user.dto.*
import goodspace.teaming.user.service.TokenManagementService
import goodspace.teaming.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

private val NO_CONTENT = ResponseEntity.noContent().build<Void>()

@RestController
@RequestMapping("/users")
@Tag(
    name = "회원 API",
    description = "회원 정보 조회 및 삭제 기능(아바타 수정은 파일 API에서 담당합니다)"
)
class UserController(
    private val userService: UserService,
    private val tokenManagementService: TokenManagementService
) {
    @GetMapping("/me")
    @Operation(
        summary = "회원 정보 조회"
    )
    fun getInfo(
        principal: Principal
    ): ResponseEntity<UserInfoResponseDto> {
        val userId = principal.userId

        val response = userService.getUserInfo(userId)

        return ResponseEntity.ok(response)
    }

    @GetMapping
    @Operation(
        summary = "다른 회원 정보 조회",
        description = "ID를 기반으로 다른 회원의 정보를 조회합니다. (방을 탈퇴한 멤버의 정보를 조회할 때 사용)"
    )
    fun getOtherInfo(
        @RequestBody requestDto: UserInfoRequestDto
    ): ResponseEntity<List<UserInfoResponseDto>> {
        val response = userService.getUserInfo(requestDto)

        return ResponseEntity.ok(response)
    }

    @PatchMapping("/me/email")
    @Operation(
        summary = "이메일 수정",
        description = "이메일을 수정합니다. 새롭게 등록할 이메일이 인증되어 있어야 합니다."
    )
    fun updateEmail(
        principal: Principal,
        @RequestBody requestDto: UpdateEmailRequestDto
    ): ResponseEntity<Void> {
        val userId = principal.userId

        userService.updateEmail(userId, requestDto)

        return NO_CONTENT
    }

    @PatchMapping("/me/password")
    @Operation(
        summary = "비밀번호 수정",
        description = "비밀번호를 수정합니다. 기존 비밀번호가 올바라야 합니다."
    )
    fun updatePassword(
        principal: Principal,
        @RequestBody requestDto: UpdatePasswordRequestDto
    ): ResponseEntity<Void> {
        val userId = principal.userId

        userService.updatePassword(userId, requestDto)

        return NO_CONTENT
    }

    @PatchMapping("/me/name")
    @Operation(
        summary = "이름 수정",
    )
    fun updateName(
        principal: Principal,
        @RequestBody requestDto: UpdateNameRequestDto
    ): ResponseEntity<Void> {
        val userId = principal.userId

        userService.updateName(userId, requestDto)

        return NO_CONTENT
    }

    @PostMapping("/me/access-token")
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

    @DeleteMapping("/me/log-out")
    @Operation(
        summary = "로그아웃(리프레쉬 토큰 만료)",
        description = "리프레쉬 토큰을 만료시킵니다. 엑세스 토큰은 클라이언트 단에서 직접 처분해야 합니다."
    )
    fun logout(
        principal: Principal
    ): ResponseEntity<Void> {
        val userId = principal.userId

        tokenManagementService.expireRefreshToken(userId)

        return NO_CONTENT
    }

    @DeleteMapping("/me/withdraw")
    @Operation(
        summary = "회원 탈퇴",
        description = "회원을 삭제합니다. 결제한 금액을 환불받지 못함을 고지해야 합니다."
    )
    fun withdraw(
        principal: Principal
    ): ResponseEntity<Void> {
        val userId = principal.userId

        userService.removeUser(userId)

        return NO_CONTENT
    }
}
