package goodspace.teaming.file.service

import goodspace.teaming.file.domain.*
import goodspace.teaming.file.dto.*
import goodspace.teaming.global.repository.RoomRepository
import goodspace.teaming.global.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.s3.model.S3Exception
import java.net.URLConnection

private const val MSG_USER_NOT_FOUND = "회원을 조회할 수 없습니다."
private const val MSG_ROOM_NOT_FOUND = "방을 조회할 수 없습니다."
private const val MSG_IMAGE_TOO_LARGE = "이미지 크기가 너무 큽니다."
private const val MSG_UNSUPPORTED_IMAGE_TYPE = "지원하지 않는 이미지 형식입니다."
private const val MSG_INVALID_UPLOAD_KEY = "업로드 키가 올바르지 않습니다."
private const val MSG_OBJECT_NOT_FOUND = "원본 객체를 찾을 수 없습니다."
private const val MSG_CHECKSUM_MISSING = "체크섬이 누락되었습니다."
private const val MSG_INVALID_IMAGE = "유효한 이미지 파일이 아닙니다."

@Service
class AvatarServiceImpl(
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository,
    private val presignedUploadUrlProvider: PresignedUploadUrlProvider,
    private val storageUrlProvider: StorageUrlProvider,
    private val s3PresignedUrlProvider: S3PresignedStorageUrlProvider,
    @Value("\${app.avatar.user-prefix:avatars/users}") private val userAvatarPrefix: String,
    @Value("\${app.avatar.room-prefix:avatars/rooms}") private val roomAvatarPrefix: String,
    @Value("\${app.avatar.max-size-mb:5}") private val maxAvatarSizeMb: Long
) : AvatarService {

    private val allowedImageTypes = setOf("image/png", "image/jpeg", "image/webp")
    private val maxBytes get() = maxAvatarSizeMb.coerceAtLeast(1) * 1024 * 1024

    private fun keyOf(ownerType: AvatarOwnerType, ownerId: Long): String = when (ownerType) {
        AvatarOwnerType.USER -> "$userAvatarPrefix/$ownerId/avatar"
        AvatarOwnerType.ROOM -> "$roomAvatarPrefix/$ownerId/avatar"
    }

    @Transactional(readOnly = true)
    override fun intent(
        ownerType: AvatarOwnerType,
        ownerId: Long,
        request: AvatarUploadIntentRequestDto
    ): AvatarUploadIntentResponseDto {
        require(request.byteSize in 1..maxBytes) { MSG_IMAGE_TOO_LARGE }

        // ⬇서명에는 원문 그대로 사용, 허용여부 판단은 소문자 기준
        val clientCtRaw = request.contentType.trim()
        val ctForAllowCheck = clientCtRaw.lowercase()
        require(ctForAllowCheck in allowedImageTypes) { "$MSG_UNSUPPORTED_IMAGE_TYPE: $clientCtRaw" }

        when (ownerType) {
            AvatarOwnerType.USER -> userRepository.findById(ownerId).orElseThrow { IllegalArgumentException(MSG_USER_NOT_FOUND) }
            AvatarOwnerType.ROOM -> roomRepository.findById(ownerId).orElseThrow { IllegalArgumentException(MSG_ROOM_NOT_FOUND) }
        }

        val objectKey = keyOf(ownerType, ownerId)
        val presigned = presignedUploadUrlProvider.putUploadUrl(
            key = objectKey,
            contentType = clientCtRaw, // ⬅원문 그대로 서명
            checksumBase64 = request.checksumSha256Base64
        )

        return AvatarUploadIntentResponseDto(
            key = objectKey,
            bucket = s3PresignedUrlProvider.bucket(),
            url = presigned.url,
            requiredHeaders = presigned.requiredHeaders // 여기에 Content-Type(원문) 포함되도록 Provider에서 내려주세요
        )
    }

    @Transactional
    override fun complete(
        ownerType: AvatarOwnerType,
        ownerId: Long,
        request: AvatarUploadCompleteRequestDto
    ): AvatarUploadCompleteResponseDto {
        val objectKey = keyOf(ownerType, ownerId)
        require(request.key == objectKey) { MSG_INVALID_UPLOAD_KEY }

        val head = try {
            s3PresignedUrlProvider.headWithChecksum(objectKey)
        } catch (e: S3Exception) {
            throw IllegalArgumentException(MSG_OBJECT_NOT_FOUND, e)
        }

        val objectSize = head.contentLength()
        require(objectSize in 1L..maxBytes) { MSG_IMAGE_TOO_LARGE }
        require(!head.checksumSHA256().isNullOrBlank()) { MSG_CHECKSUM_MISSING }

        // ⬇매직바이트로 서버가 타입을 판정(최소 앞 1KB만 내려받아 검사)
        val headBytes = s3PresignedUrlProvider.getRangeBytes(objectKey, 0, 1023)
        val sniffed = detectImageTypeByMagicBytes(headBytes)
        val serverCt = sniffed ?: guessContentTypeFromKey(objectKey)
        require(serverCt in allowedImageTypes) { "$MSG_UNSUPPORTED_IMAGE_TYPE: $serverCt" }

        // ⬇최종 키에 "메타데이터 교체(copy-in-place)"로 서버 판정 Content-Type/헤더를 강제
        s3PresignedUrlProvider.rewriteObjectMetadata(
            key = objectKey,
            contentType = serverCt,
            cacheControl = "public, max-age=31536000, immutable",
            contentDisposition = "inline; filename=\"avatar.${extOf(serverCt)}\""
        )

        val (version, publicUrl) = when (ownerType) {
            AvatarOwnerType.USER -> {
                val user = userRepository.findById(ownerId).orElseThrow { IllegalArgumentException(MSG_USER_NOT_FOUND) }
                user.avatarKey = objectKey
                user.avatarVersion += 1
                val url = storageUrlProvider.publicUrl(objectKey, version = user.avatarVersion)
                user.avatarVersion to (url ?: "/static/default-avatar.png")
            }
            AvatarOwnerType.ROOM -> {
                val room = roomRepository.findById(ownerId).orElseThrow { IllegalArgumentException(MSG_ROOM_NOT_FOUND) }
                room.avatarKey = objectKey
                room.avatarVersion = (room.avatarVersion ?: 0) + 1
                val url = storageUrlProvider.publicUrl(objectKey, version = room.avatarVersion!!)
                room.avatarVersion!! to (url ?: "/static/default-avatar.png")
            }
        }

        return AvatarUploadCompleteResponseDto(
            avatarKey = objectKey,
            avatarVersion = version,
            publicUrl = publicUrl
        )
    }

    @Transactional(readOnly = true)
    override fun issueViewUrl(ownerType: AvatarOwnerType, ownerId: Long): AvatarUrlResponseDto {
        val (key, version) = when (ownerType) {
            AvatarOwnerType.USER -> {
                val user = userRepository.findById(ownerId).orElseThrow { IllegalArgumentException(MSG_USER_NOT_FOUND) }
                user.avatarKey to user.avatarVersion
            }
            AvatarOwnerType.ROOM -> {
                val room = roomRepository.findById(ownerId).orElseThrow { IllegalArgumentException(MSG_ROOM_NOT_FOUND) }
                room.avatarKey to (room.avatarVersion ?: 0)
            }
        }
        val url = storageUrlProvider.publicUrl(key, version = version) ?: "/static/default-avatar.png"
        return AvatarUrlResponseDto(url)
    }

    @Transactional
    override fun delete(ownerType: AvatarOwnerType, ownerId: Long) {
        when (ownerType) {
            AvatarOwnerType.USER -> {
                val user = userRepository.findById(ownerId).orElseThrow { IllegalArgumentException(MSG_USER_NOT_FOUND) }
                user.avatarKey = null
                user.avatarVersion = 0
            }
            AvatarOwnerType.ROOM -> {
                val room = roomRepository.findById(ownerId).orElseThrow { IllegalArgumentException(MSG_ROOM_NOT_FOUND) }
                room.avatarKey = null
                room.avatarVersion = 0
            }
        }
    }

    private fun guessContentTypeFromKey(key: String): String {
        val name = key.substringAfterLast('/')
        return URLConnection.guessContentTypeFromName(name)?.lowercase() ?: "application/octet-stream"
    }

    private fun detectImageTypeByMagicBytes(bytes: ByteArray): String? {
        if (bytes.size >= 8 &&
            bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() &&
            bytes[3] == 0x47.toByte() && bytes[4] == 0x0D.toByte() && bytes[5] == 0x0A.toByte() &&
            bytes[6] == 0x1A.toByte() && bytes[7] == 0x0A.toByte()
        ) return "image/png"

        if (bytes.size >= 3 &&
            bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() && bytes[2] == 0xFF.toByte()
        ) return "image/jpeg"

        if (bytes.size >= 12) {
            val riff = String(bytes.copyOfRange(0, 4))
            val webp = String(bytes.copyOfRange(8, 12))
            if (riff == "RIFF" && webp == "WEBP") return "image/webp"
        }
        return null
    }

    private fun extOf(contentType: String) = when (contentType.lowercase()) {
        "image/png" -> "png"
        "image/jpeg" -> "jpg"
        "image/webp" -> "webp"
        else -> "bin"
    }
}
