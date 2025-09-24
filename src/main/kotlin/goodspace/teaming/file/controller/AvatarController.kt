package goodspace.teaming.file.controller

import goodspace.teaming.file.domain.AvatarOwnerType
import goodspace.teaming.file.dto.*
import goodspace.teaming.file.service.AvatarService
import goodspace.teaming.global.security.getUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/users/me/avatar")
@Tag(
    name = "아바타 API",
    description = "사용자 프로필 이미지 업로드/확정/조회/삭제 API"
)
class AvatarController(
    private val avatarService: AvatarService
) {
    @PostMapping("/intent")
    @Operation(
        summary = "아바타 업로드 사전 준비 (Presigned PUT 발급)",
        description = """
            S3로 직접 업로드하기 위한 Presigned PUT URL을 발급합니다.
            
            클라이언트는 응답의 `url`로 파일을 PUT 업로드하고, 반드시 다음 헤더를 포함해야 합니다:
            - `Content-Type`: 요청 바디의 `contentType`와 동일
            - `x-amz-checksum-sha256`: 파일 바이트의 SHA-256(Base64)
            
            업로드 완료 후 `/users/me/avatar/complete`를 호출해 확정하세요.
            
            참고:
            - 서버는 HEAD Object로 업로드 존재 및 헤더(크기/타입/체크섬)를 검증합니다.
            - 체크섬은 클라이언트가 직접 계산하여 intent 요청에 담아야 합니다.
        """
    )
    fun intent(
        principal: Principal,
        @RequestBody requestDto: AvatarUploadIntentRequestDto
    ): ResponseEntity<AvatarUploadIntentResponseDto> {
        val userId = principal.getUserId()
        val res = avatarService.intent(AvatarOwnerType.USER, userId, requestDto)
        return ResponseEntity.ok(res)
    }

    @PostMapping("/complete")
    @Operation(
        summary = "아바타 업로드 확정",
        description = """
            S3에 PUT 업로드가 완료된 후 호출합니다.
            서버는 S3 HEAD로 객체의 존재/헤더를 검증하고, 사용자 프로필(`avatarKey`, `avatarVersion`)을 갱신합니다.
            
            반환 `publicUrl`은 현재 구성된 StorageUrlProvider에 따라 달라집니다:
            - Presigned 전략: 일시적 GET URL
            - CDN 전략: `cdn.base/{key}?v={avatarVersion}` 형태 (캐시 무효화용 버전 파라미터 포함)
        """
    )
    fun complete(
        principal: Principal,
        @RequestBody requestDto: AvatarUploadCompleteRequestDto
    ): ResponseEntity<AvatarUploadCompleteResponseDto> {
        val userId = principal.getUserId()
        val res = avatarService.complete(AvatarOwnerType.USER, userId, requestDto)
        return ResponseEntity.ok(res)
    }

    @PostMapping("/url")
    @Operation(
        summary = "아바타 보기용 URL 발급",
        description = """
            현재 사용자의 아바타를 표시하기 위한 URL을 발급합니다.
            구성에 따라 Presigned GET 또는 CDN URL이 반환됩니다.
            기본 아바타가 없으면 서버의 기본 경로(`/static/default-avatar.png`)가 반환됩니다.
        """
    )
    fun issueViewUrl(
        principal: Principal
    ): ResponseEntity<AvatarUrlResponseDto> {
        val userId = principal.getUserId()
        val res = avatarService.issueViewUrl(AvatarOwnerType.USER, userId)
        return ResponseEntity.ok(res)
    }

    @DeleteMapping
    @Operation(
        summary = "아바타 삭제",
        description = """
            사용자의 아바타 메타데이터를 초기화합니다.
            (실제 S3 객체 삭제는 별도 정책/배치에서 처리할 수 있습니다)
        """
    )
    fun delete(
        principal: Principal
    ): ResponseEntity<Void> {
        val userId = principal.getUserId()
        avatarService.delete(AvatarOwnerType.USER, userId)
        return ResponseEntity.noContent().build()
    }
}
