package goodspace.teaming.file.domain

import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.stereotype.Component

@Component
class FileValidation(
    private val userRoomRepository: UserRoomRepository
) {
    fun requireMembership(userId: Long, roomId: Long) {
        require(userRoomRepository.existsByRoomIdAndUserId(roomId, userId)) { "해당 방 소속이 아닙니다." }
    }
    fun validateDeclaredSize(size: Long) {
        require(size in FileConstants.MIN_OBJECT_BYTES..(FileConstants.MAX_UPLOAD_SIZE_MB * FileConstants.BYTES_PER_MB)) { "파일 크기가 너무 큽니다." }
    }
    fun validateStoredSize(size: Long) {
        require(size in FileConstants.MIN_OBJECT_BYTES..(FileConstants.MAX_UPLOAD_SIZE_MB * FileConstants.BYTES_PER_MB)) { "객체 크기가 적절하지 않습니다." }
    }
    fun validateAllowedContentType(mime: String) {
        require(FileConstants.ALLOWED_MIME_PREFIXES.any { mime.startsWith(it) }) { "지원하지 않는 Content-Type 입니다." }
    }
}
