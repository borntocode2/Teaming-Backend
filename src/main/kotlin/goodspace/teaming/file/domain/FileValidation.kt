package goodspace.teaming.file.domain

import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.stereotype.Component

private const val MSG_NOT_ROOM_MEMBER = "해당 방 소속이 아닙니다."
private const val MSG_SIZE_TOO_LARGE = "파일 크기가 너무 큽니다."
private const val MSG_INVALID_OBJECT_SIZE = "객체 크기가 적절하지 않습니다."
private const val MSG_UNSUPPORTED_CONTENT_TYPE = "지원하지 않는 Content-Type 입니다."

@Component
class FileValidation(
    private val userRoomRepository: UserRoomRepository
) {
    fun requireMembership(userId: Long, roomId: Long) {
        require(userRoomRepository.existsByRoomIdAndUserId(roomId, userId)) { MSG_NOT_ROOM_MEMBER }
    }

    fun validateDeclaredSize(size: Long) {
        require(size in FileConstants.MIN_OBJECT_BYTES..(FileConstants.MAX_UPLOAD_SIZE_MB * FileConstants.BYTES_PER_MB)) { MSG_SIZE_TOO_LARGE }
    }

    fun validateStoredSize(size: Long) {
        require(size in FileConstants.MIN_OBJECT_BYTES..(FileConstants.MAX_UPLOAD_SIZE_MB * FileConstants.BYTES_PER_MB)) { MSG_INVALID_OBJECT_SIZE }
    }

    fun validateAllowedContentType(mime: String) {
        require(FileConstants.ALLOWED_MIME_PREFIXES.any { mime.startsWith(it) }) { MSG_UNSUPPORTED_CONTENT_TYPE }
    }
}
