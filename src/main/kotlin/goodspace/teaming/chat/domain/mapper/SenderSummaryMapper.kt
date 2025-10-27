package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.SenderSummaryResponseDto
import goodspace.teaming.file.domain.CdnStorageUrlProvider
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.file.domain.StorageUrlProvider
import org.springframework.stereotype.Component

@Component
class SenderSummaryMapper(
    private val urlProvider: CdnStorageUrlProvider
) {
    fun map(user: User?, size: Int = 64): SenderSummaryResponseDto? {
        if (user == null) {
            return null
        }

        return SenderSummaryResponseDto(
            id = user.id,
            name = user.name,
            avatarUrl = urlProvider.publicUrl(
                key = user.avatarKey,
                version = user.avatarVersion,
                size = size
            ),
            avatarVersion = user.avatarVersion
        )
    }
}
