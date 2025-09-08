package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.SenderSummaryDto
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.storage.StorageUrlProvider
import org.springframework.stereotype.Component

@Component
class SenderSummaryMapper(
    private val urlProvider: StorageUrlProvider
) {
    fun map(user: User, size: Int = 64): SenderSummaryDto {
        return SenderSummaryDto(
            id = user.id,
            name = user.name,
            avatarUrl = urlProvider.publicUrl(
                key = user.avatarKey,
                version = user.avatarVersion,
                size = size
            )
        )
    }
}
