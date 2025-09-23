package goodspace.teaming.file.controller

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
            클라이언트는 응답의 `url`로 파일을 PUT 업로드하고, 반드시 다음 헤더를 포함해야 합니다.
            - `Content-Type`: 요청에 보낸 contentType과 동일
            - `x-amz-checksum-sha256`: 파일 바이트의 SHA-256(Base64)
            
            업로드 완료 후 `/users/me/avatar/complete`를 호출해 확정하세요.
        """
    )
    fun intent(
        principal: Principal,
        @RequestBody requestDto: AvatarUploadIntentRequestDto
    ): ResponseEntity<AvatarUploadIntentResponseDto> {
        val userId = principal.getUserId()
        return ResponseEntity.ok(avatarService.intent(userId, requestDto))
    }

    @PostMapping("/complete")
    @Operation(
        summary = "아바타 업로드 확정",
        description = """
            S3에 PUT 업로드가 완료된 후 호출합니다.
            서버는 S3 HEAD로 객체의 존재/헤더를 검증하고, 사용자 프로필(`avatarKey`, `avatarVersion`)을 갱신합니다.
            반환되는 `publicUrl`은 CDN 또는 Presigned GET URL일 수 있으며, `?v=avatarVersion` 쿼리로 캐시 무효화가 가능합니다.
        """
    )
    fun complete(
        principal: Principal,
        @RequestBody requestDto: AvatarUploadCompleteRequestDto
    ): ResponseEntity<AvatarUploadCompleteResponseDto> {
        val userId = principal.getUserId()
        return ResponseEntity.ok(avatarService.complete(userId, requestDto))
    }

    @PostMapping("/url")
    @Operation(
        summary = "아바타 보기용 URL 발급",
        description = """
            현재 사용자의 아바타를 표시하기 위한 URL을 발급합니다.
            아니면 Presigned GET URL을 반환할 수 있습니다.
        """
    )
    fun issueViewUrl(
        principal: Principal
    ): ResponseEntity<AvatarUrlResponseDto> {
        val userId = principal.getUserId()
        return ResponseEntity.ok(avatarService.issueViewUrl(userId))
    }

    @DeleteMapping
    @Operation(
        summary = "아바타 삭제",
        description = """
            사용자의 아바타 메타데이터를 초기화합니다. 
            (실제 S3 삭제 여부는 서비스 정책에 따릅니다)
        """
    )
    fun delete(
        principal: Principal
    ): ResponseEntity<Void> {
        val userId = principal.getUserId()
        avatarService.delete(userId)
        return ResponseEntity.noContent().build()
    }
}
