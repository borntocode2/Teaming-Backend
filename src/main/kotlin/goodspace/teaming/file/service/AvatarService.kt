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
    private val s3PresignSupport: S3PresignSupport,
    @Value("\${app.avatar.prefix:avatars}") private val avatarPrefix: String,
    @Value("\${app.avatar.max-size-mb:5}") private val maxAvatarSizeMb: Long
) {
    private val allowedImageTypes = setOf("image/png", "image/jpeg", "image/webp")
    private val maxBytes get() = maxAvatarSizeMb.coerceAtLeast(1) * 1024 * 1024

    private fun keyOf(userId: Long) = "$avatarPrefix/$userId/avatar"

    @Transactional(readOnly = true)
    fun intent(userId: Long, requestDto: AvatarUploadIntentRequestDto): AvatarUploadIntentResponseDto {
        require(requestDto.byteSize in 1..maxBytes) { "아바타 최대 크기는 ${maxAvatarSizeMb}MB 입니다." }

        val normalizedContentType = normalizeContentType(requestDto.contentType)
        require(normalizedContentType in allowedImageTypes) { "지원하지 않는 이미지 형식입니다: $normalizedContentType" }

        val objectKey = keyOf(userId)
        val presignedPutUrl = s3PresignSupport.presignPut(objectKey, normalizedContentType)
        val requiredHeaders = mapOf(
            "Content-Type" to normalizedContentType,
            "x-amz-checksum-sha256" to requestDto.checksumSha256Base64
        )

        return AvatarUploadIntentResponseDto(
            key = objectKey,
            bucket = s3PresignSupport.bucket(),
            url = presignedPutUrl,
            requiredHeaders = requiredHeaders
        )
    }

    @Transactional
    fun complete(userId: Long, requestDto: AvatarUploadCompleteRequestDto): AvatarUploadCompleteResponseDto {
        val objectKey = keyOf(userId)
        require(requestDto.key == objectKey) { "key 불일치: intent에서 발급한 key만 허용됩니다." }

        val head = try {
            s3PresignSupport.headWithChecksum(objectKey)
        } catch (e: S3Exception) {
            throw IllegalArgumentException("S3에 업로드된 아바타 원본이 존재하지 않습니다.", e)
        }

        val objectSize = head.contentLength()
        require(objectSize in 1L..maxBytes) { "아바타 최대 크기는 ${maxAvatarSizeMb}MB 입니다." }

        val contentType = head.contentType() ?: guessContentTypeFromKey(objectKey)
        require(contentType in allowedImageTypes) { "지원하지 않는 이미지 형식입니다: $contentType" }
        require(!head.checksumSHA256().isNullOrBlank()) { "체크섬이 누락되었습니다." }

        val user = userRepository.findById(userId).orElseThrow()
        user.avatarKey = objectKey
        user.avatarVersion++

        val (presignedGetUrl, _) = s3PresignSupport.presignGetWithDisposition(
            key = objectKey,
            filename = "avatar-$userId",
            contentType = contentType
        )

        return AvatarUploadCompleteResponseDto(
            avatarKey = objectKey,
            avatarVersion = user.avatarVersion,
            publicUrl = presignedGetUrl
        )
    }

    @Transactional(readOnly = true)
    fun issueViewUrl(userId: Long): AvatarUrlResponseDto {
        val user = userRepository.findById(userId).orElseThrow()
        val objectKey = user.avatarKey

        if (objectKey.isNullOrBlank()) {
            return AvatarUrlResponseDto(url = defaultAvatarUrl())
        }

        val (presignedGetUrl, _) = s3PresignSupport.presignGetWithDisposition(
            key = objectKey,
            filename = "avatar-$userId",
            contentType = null
        )
        return AvatarUrlResponseDto(url = presignedGetUrl)
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

    private fun defaultAvatarUrl(): String = "/static/default-avatar.png"
}
