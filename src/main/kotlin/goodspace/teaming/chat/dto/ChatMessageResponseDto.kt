package goodspace.teaming.chat.dto

import goodspace.teaming.global.entity.room.MessageType
import java.time.LocalDateTime

data class ChatMessageResponseDto(
    val messageId: Long,
    val roomId: Long,
    val clientMessageId: String,
    val type: MessageType,
    val content: String?,
    val createdAt: LocalDateTime,
    val sender: SenderSummaryDto,
    val attachments: List<MessageAttachmentDto> = emptyList()
)
