package goodspace.teaming.file.dto

import goodspace.teaming.file.domain.AvatarOwnerType

data class AvatarUploadCompleteRequestDto(
    val key: String,
    val width: Int? = null,
    val height: Int? = null,
    val ownerType: AvatarOwnerType
)
