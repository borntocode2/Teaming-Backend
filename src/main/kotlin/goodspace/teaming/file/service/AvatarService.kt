package goodspace.teaming.file.service

import goodspace.teaming.file.dto.*

interface AvatarService {
    fun intent(
        ownerId: Long,
        requestDto: AvatarUploadIntentRequestDto
    ): AvatarUploadIntentResponseDto

    fun complete(
        ownerId: Long,
        requestDto: AvatarUploadCompleteRequestDto
    ): AvatarUploadCompleteResponseDto

    fun issueViewUrl(
        ownerId: Long,
        ownerTypeDto: AvatarOwnerTypeDto
    ): AvatarUrlResponseDto

    fun delete(
        ownerId: Long,
        ownerTypeDto: AvatarOwnerTypeDto
    )
}
