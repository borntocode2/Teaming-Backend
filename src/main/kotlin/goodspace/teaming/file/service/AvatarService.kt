package goodspace.teaming.file.service

import goodspace.teaming.file.dto.*

interface AvatarService {
    fun intent(
        userId: Long,
        requestDto: AvatarUploadIntentRequestDto
    ): AvatarUploadIntentResponseDto

    fun complete(
        userId: Long,
        requestDto: AvatarUploadCompleteRequestDto
    ): AvatarUploadCompleteResponseDto

    fun issueViewUrl(
        userId: Long,
        ownerTypeDto: AvatarOwnerTypeDto
    ): AvatarUrlResponseDto

    fun delete(
        userId: Long,
        ownerTypeDto: AvatarOwnerTypeDto
    )
}
