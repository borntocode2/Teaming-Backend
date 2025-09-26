package goodspace.teaming.user.domain.mapper

import goodspace.teaming.file.domain.CdnStorageUrlProvider
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.user.dto.UserInfoResponseDto
import org.springframework.stereotype.Component

@Component
class UserInfoMapper(
    private val storageUrlProvider: CdnStorageUrlProvider
) {
    fun map(user: User): UserInfoResponseDto {
        return UserInfoResponseDto(
            email = user.email,
            name = user.name,
            avatarUrl = storageUrlProvider.publicUrl(user.avatarKey, user.avatarVersion),
            avatarVersion = user.avatarVersion
        )
    }
}
