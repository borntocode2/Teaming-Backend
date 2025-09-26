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

    private fun keyOf(ownerType: AvatarOwnerType, userId: Long, roomId: Long?): String = when (ownerType) {
        AvatarOwnerType.USER -> "$userAvatarPrefix/$userId/avatar"
        AvatarOwnerType.ROOM -> "$roomAvatarPrefix/$roomId/avatar"
    }

    @Transactional(readOnly = true)
    override fun intent(
        userId: Long,
        requestDto: AvatarUploadIntentRequestDto
    ): AvatarUploadIntentResponseDto {
        val ownerType = requestDto.ownerType
        require(requestDto.byteSize in 1..maxBytes) { MSG_IMAGE_TOO_LARGE }

        val clientCtRaw = requestDto.contentType.trim()
        val ctForAllowCheck = clientCtRaw.lowercase()
        require(ctForAllowCheck in allowedImageTypes) { "$MSG_UNSUPPORTED_IMAGE_TYPE: $clientCtRaw" }

        when (ownerType) {
            AvatarOwnerType.USER -> userRepository.findById(userId).orElseThrow { IllegalArgumentException(MSG_USER_NOT_FOUND) }
            AvatarOwnerType.ROOM -> roomRepository.findById(requestDto.roomId!!).orElseThrow { IllegalArgumentException(MSG_ROOM_NOT_FOUND) }
        }

        val objectKey = keyOf(ownerType, userId, requestDto.roomId)
        val presigned = presignedUploadUrlProvider.putUploadUrl(
            key = objectKey,
            contentType = clientCtRaw
        )

        return AvatarUploadIntentResponseDto(
            key = objectKey,
            bucket = s3PresignedUrlProvider.bucket(),
            url = presigned.url,
            requiredHeaders = presigned.requiredHeaders
        )
    }

    @Transactional
    override fun complete(
        userId: Long,
        requestDto: AvatarUploadCompleteRequestDto
    ): AvatarUploadCompleteResponseDto {
        val ownerType = requestDto.ownerType

        val objectKey = keyOf(ownerType, userId, requestDto.roomId)
        require(requestDto.key == objectKey) { MSG_INVALID_UPLOAD_KEY }

        val head = try {
            s3PresignedUrlProvider.head(objectKey)
        } catch (e: S3Exception) {
            throw IllegalArgumentException(MSG_OBJECT_NOT_FOUND, e)
        }

        val objectSize = head.contentLength()
        require(objectSize in 1L..maxBytes) { MSG_IMAGE_TOO_LARGE }

        // 매직바이트로 서버가 타입 판정
        val headBytes = s3PresignedUrlProvider.getRangeBytes(objectKey, 0, 1023)
        val sniffed = detectImageTypeByMagicBytes(headBytes)
        val serverCt = sniffed ?: guessContentTypeFromKey(objectKey)
        require(serverCt in allowedImageTypes) { "$MSG_UNSUPPORTED_IMAGE_TYPE: $serverCt" }

        // 최종 메타데이터 교체
        s3PresignedUrlProvider.rewriteObjectMetadata(
            key = objectKey,
            contentType = serverCt,
            cacheControl = "public, max-age=31536000, immutable",
            contentDisposition = "inline; filename=\"avatar.${extOf(serverCt)}\""
        )

        val (version, publicUrl) = when (ownerType) {
            AvatarOwnerType.USER -> {
                val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException(MSG_USER_NOT_FOUND) }
                user.avatarKey = objectKey
                user.avatarVersion++
                val url = storageUrlProvider.publicUrl(objectKey, version = user.avatarVersion)
                user.avatarVersion to (url ?: "/static/default-avatar.png")
            }
            AvatarOwnerType.ROOM -> {
                val room = roomRepository.findById(requestDto.roomId!!).orElseThrow { IllegalArgumentException(MSG_ROOM_NOT_FOUND) }
                room.avatarKey = objectKey
                room.avatarVersion++
                val url = storageUrlProvider.publicUrl(objectKey, version = room.avatarVersion)
                room.avatarVersion to (url ?: "/static/default-avatar.png")
            }
        }

        return AvatarUploadCompleteResponseDto(
            avatarKey = objectKey,
            avatarVersion = version,
            publicUrl = publicUrl
        )
    }

    @Transactional(readOnly = true)
    override fun issueViewUrl(userId: Long, ownerTypeDto: AvatarOwnerTypeDto): AvatarUrlResponseDto {
        val ownerType = ownerTypeDto.ownerType

        val (key, version) = when (ownerType) {
            AvatarOwnerType.USER -> {
                val user = userRepository.findById(userId).orElseThrow { IllegalArgumentException(MSG_USER_NOT_FOUND) }
                user.avatarKey to user.avatarVersion
            }
            AvatarOwnerType.ROOM -> {
                val room = roomRepository.findById(ownerTypeDto.roomId!!).orElseThrow { IllegalArgumentException(MSG_ROOM_NOT_FOUND) }
                room.avatarKey to (room.avatarVersion)
            }
        }
        val url = storageUrlProvider.publicUrl(key, version = version)

        return AvatarUrlResponseDto(url)
    }

    @Transactional
    override fun delete(userId: Long, ownerTypeDto: AvatarOwnerTypeDto) {
        val ownerType = ownerTypeDto.ownerType

        when (ownerType) {
            AvatarOwnerType.USER -> {
                val user = userRepository.findById(userId)
                    .orElseThrow { IllegalArgumentException(MSG_USER_NOT_FOUND) }
                user.avatarKey = null
                user.avatarVersion = 0
            }
            AvatarOwnerType.ROOM -> {
                val room = roomRepository.findById(ownerTypeDto.roomId!!)
                    .orElseThrow { IllegalArgumentException(MSG_ROOM_NOT_FOUND) }
                room.avatarKey = null
                room.avatarVersion = 0
            }
        }
    }

    // --- 이하 유틸 그대로 ---
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
