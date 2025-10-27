package goodspace.teaming.chat.dto

import goodspace.teaming.global.entity.room.MessageType
import java.time.Instant

data class LastMessagePreviewResponseDto(
    val id: Long,
    val type: MessageType,
    val content: String?,
    val sender: SenderSummaryResponseDto?,
    val createdAt: Instant
)
