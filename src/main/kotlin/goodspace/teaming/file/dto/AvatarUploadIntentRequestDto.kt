package goodspace.teaming.file.dto

import goodspace.teaming.file.domain.AvatarOwnerType

data class AvatarUploadIntentRequestDto(
    val contentType: String,
    val byteSize: Long,
    val ownerType: AvatarOwnerType
)
