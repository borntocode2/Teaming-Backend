package goodspace.teaming.file.service

import goodspace.teaming.file.domain.AvatarOwnerType
import goodspace.teaming.file.dto.*

interface AvatarService {
    fun intent(
        ownerType: AvatarOwnerType,
        ownerId: Long,
        request: AvatarUploadIntentRequestDto
    ): AvatarUploadIntentResponseDto

    fun complete(
        ownerType: AvatarOwnerType,
        ownerId: Long,
        request: AvatarUploadCompleteRequestDto
    ): AvatarUploadCompleteResponseDto

    fun issueViewUrl(
        ownerType: AvatarOwnerType,
        ownerId: Long
    ): AvatarUrlResponseDto

    fun delete(
        ownerType: AvatarOwnerType,
        ownerId: Long
    )
}
