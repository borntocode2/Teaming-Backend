package goodspace.teaming.file.service

import goodspace.teaming.file.domain.S3PresignSupport
import goodspace.teaming.file.dto.*
import goodspace.teaming.global.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.s3.model.S3Exception
import java.net.URLConnection

private const val USER_NOT_FOUND = "회원을 조회할 수 없습니다."

@Service
class AvatarService(
    private val userRepository: UserRepository,
    private val s3: S3PresignSupport,
    @Value("\${app.avatar.prefix:avatars}") private val avatarPrefix: String,
    @Value("\${app.avatar.max-size-mb:5}") private val maxAvatarSizeMb: Long,
    @Value("\${app.cdn.base-url:}") private val cdnBaseUrl: String, // 비어있으면 presigned GET 사용
) {
    private val allowedImageTypes = setOf("image/png", "image/jpeg", "image/webp")
    private val maxBytes get() = maxAvatarSizeMb.coerceAtLeast(1) * 1024 * 1024

    private fun keyOf(userId: Long) = "$avatarPrefix/$userId/avatar"

    @Transactional(readOnly = true)
    fun intent(userId: Long, requestDto: AvatarUploadIntentRequestDto): AvatarUploadIntentResponseDto {
        // 1) 용량 검증
        require(requestDto.byteSize in 1..maxBytes) { "아바타 최대 크기는 ${maxAvatarSizeMb}MB 입니다." }

        // 2) Content-Type 정규화 + 화이트리스트
        val normalized = normalizeContentType(requestDto.contentType)
        require(normalized in allowedImageTypes) { "지원하지 않는 이미지 형식입니다: $normalized" }

        // 3) 고정 Key (덮어쓰기)
        val key = keyOf(userId)

        // 4) Presigned PUT 발급 (x-amz-checksum-sha256 요구됨: S3PresignSupport가 강제)
        val url = s3.presignPut(key, normalized)
        val headers = mapOf(
            "Content-Type" to normalized,
            "x-amz-checksum-sha256" to requestDto.checksumSha256Base64
        )

        return AvatarUploadIntentResponseDto(
            key = key,
            bucket = s3.bucket(),
            url = url,
            requiredHeaders = headers
        )
    }

    @Transactional
    fun complete(userId: Long, requestDto: AvatarUploadCompleteRequestDto): AvatarUploadCompleteResponseDto {
        val key = keyOf(userId)
        require(requestDto.key == key) { "key 불일치: intent에서 발급한 key만 허용됩니다." }

        // 1) S3 HEAD로 업로드 검증 (+ checksum/mime)
        val head = try {
            s3.headWithChecksum(key)
        } catch (e: S3Exception) {
            throw IllegalArgumentException("S3에 업로드된 아바타 원본이 존재하지 않습니다.", e)
        }

        val size = head.contentLength()
        require(size in 1L..maxBytes) { "아바타 최대 크기는 ${maxAvatarSizeMb}MB 입니다." }

        val mime = head.contentType() ?: guessContentTypeFromKey(key)
        require(mime in allowedImageTypes) { "지원하지 않는 이미지 형식입니다: $mime" }

        require(!head.checksumSHA256().isNullOrBlank()) { "체크섬이 누락되었습니다." }

        // 2) 유저 엔티티 갱신 (avatarVersion++)
        val user = userRepository.findById(userId).orElseThrow()
        user.avatarKey = key
        user.avatarVersion++

        // 3) 공개 URL 만들기 (CDN 사용 권장; 없으면 presigned GET 사용)
        val publicUrl = if (cdnBaseUrl.isNotBlank()) {
            // CDN 캐시 무효화: ?v={avatarVersion}
            "${cdnBaseUrl.trimEnd('/')}/$key?v=${user.avatarVersion}"
        } else {
            // 이미지 인라인 렌더링을 위해 disposition 미지정 presign이 있으면 더 좋지만,
            // 기존 유틸에 맞춰 attachment로 내려도 무방합니다.
            val (url, _) = s3.presignGetWithDisposition(
                key = key,
                filename = "avatar-$userId",
                contentType = mime
            )
            url
        }

        return AvatarUploadCompleteResponseDto(
            avatarKey = key,
            avatarVersion = user.avatarVersion,
            publicUrl = publicUrl
        )
    }

    @Transactional(readOnly = true)
    fun issueViewUrl(userId: Long): AvatarUrlResponseDto {
        val user = userRepository.findById(userId).orElseThrow()
        val key = user.avatarKey
        val version = user.avatarVersion

        if (key.isNullOrBlank()) {
            // 기본 이미지 경로: CDN에 기본 이미지가 있다면 해당 경로로 반환
            // (없다면 정적 리소스 경로나 프론트에서 placeholder 사용)
            return AvatarUrlResponseDto(url = defaultAvatarUrl())
        }

        val url = if (cdnBaseUrl.isNotBlank()) {
            "${cdnBaseUrl.trimEnd('/')}/$key?v=$version"
        } else {
            val (presigned, _) = s3.presignGetWithDisposition(
                key = key,
                filename = "avatar-$userId",
                contentType = null
            )
            presigned
        }

        return AvatarUrlResponseDto(url = url)
    }

    @Transactional
    fun delete(userId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException(USER_NOT_FOUND) }

        user.avatarKey = null
        user.avatarVersion = 0
    }

    private fun normalizeContentType(contentType: String): String {
        val ct = contentType.trim().lowercase()
        return if (ct.isNotBlank()) ct else "application/octet-stream"
    }

    private fun guessContentTypeFromKey(key: String): String {
        val name = key.substringAfterLast('/')
        return URLConnection.guessContentTypeFromName(name)?.lowercase() ?: "application/octet-stream"
    }

    private fun defaultAvatarUrl(): String =
        if (cdnBaseUrl.isNotBlank()) "${cdnBaseUrl.trimEnd('/')}/static/default-avatar.png"
        else "/static/default-avatar.png"
}
