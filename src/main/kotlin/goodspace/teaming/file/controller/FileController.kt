package goodspace.teaming.file.controller

import goodspace.teaming.file.dto.*
import goodspace.teaming.file.service.FileDownloadService
import goodspace.teaming.file.service.FileUploadService
import goodspace.teaming.global.security.getUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/files")
@Tag(
    name = "파일 API",
    description = "파일 업로드 관련"
)
class FileController(
    private val fileUploadService: FileUploadService,
    private val fileDownloadService: FileDownloadService
) {
    @PostMapping("/intent/{roomId}")
    @Operation(
        summary = "파일 업로드 사전 준비",
        description = """
            S3에 PUT 업로드할 때 필요한 Presigned URL을 발급합니다.
            다음 절차로 S3에 업로드합니다.
            1. 해당 API를 호출해 key와 url을 발급받는다.
            2. 응답받은 url로 S3에 PUT 한다
            3. 이 때, 요청 헤더에 반드시 다음을 포함한다.
                - `Content-Type`: 해당 API 요청에 보낸 것과 똑같은 내용
                - `x-amz-checksum-sha256`: 파일 바이트의 SHA-256(Base64)
            4. 업로드 성공 후 `파일 업로드 확정` API를 호출해, 파일 업로드를 서버에 통보한다.
            """
    )
    fun intent(
        principal: Principal,
        @PathVariable roomId: Long,
        @RequestBody requestDto: FileUploadIntentRequestDto
    ): ResponseEntity<FileUploadIntentResponseDto> {
        val userId = principal.getUserId()

        val response = fileUploadService.intent(userId, roomId, requestDto)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/complete/{roomId}")
    @Operation(
        summary = "파일 업로드 확정",
        description = "S3에 PUT한 후 호출하여, 파일 업로드를 서버에 확정합니다."
    )
    fun complete(
        principal: Principal,
        @PathVariable roomId: Long,
        @RequestBody requestDto: FileUploadCompleteRequestDto
    ): ResponseEntity<FileUploadCompleteResponseDto> {
        val userId = principal.getUserId()

        val response = fileUploadService.complete(userId, roomId, requestDto)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/download-url/{fileId}")
    @Operation(
        summary = "파일 다운로드 URL 발급",
        description = "인증/인가 후 S3에 직접 접근 가능한 Presigned GET URL을 발급합니다."
    )
    fun issueDownloadUrl(
        principal: Principal,
        @PathVariable fileId: Long
    ): ResponseEntity<DownloadUrlResponseDto> {
        val userId = principal.getUserId()

        val response = fileDownloadService.issueDownloadUrl(userId, fileId)

        return ResponseEntity.ok(response)
    }
}
