package goodspace.teaming.user.domain.mapper

import goodspace.teaming.global.entity.user.User
import goodspace.teaming.user.dto.UserInfoResponseDto
import org.springframework.stereotype.Component

@Component
class UserInfoMapper {
    fun map(user: User): UserInfoResponseDto {
        return UserInfoResponseDto(
            email = user.email,
            name = user.name,
            avatarKey = user.avatarKey,
            avatarVersion = user.avatarVersion
        )
    }
}
