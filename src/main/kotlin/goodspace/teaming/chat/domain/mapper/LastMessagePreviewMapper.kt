package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.LastMessagePreviewResponseDto
import goodspace.teaming.global.entity.room.Message
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class LastMessagePreviewMapper(
    private val senderSummaryMapper: SenderSummaryMapper,
    private val zoneId: ZoneId = ZoneId.systemDefault()
) {
    fun map(message: Message): LastMessagePreviewResponseDto {
        return LastMessagePreviewResponseDto(
            id = message.id!!,
            type = message.type,
            content = message.content,
            sender = senderSummaryMapper.map(message.sender),
            createdAt = message.createdAt!!.atZone(zoneId)
                .toInstant()
        )
    }
}
