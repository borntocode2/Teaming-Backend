package goodspace.teaming.chat.dto

import goodspace.teaming.global.entity.room.MessageType
import java.time.Instant

data class ChatMessageResponseDto(
    val messageId: Long,
    val roomId: Long,
    val clientMessageId: String,
    val type: MessageType,
    val content: String?,
    val createdAt: Instant,
    val sender: SenderSummaryResponseDto?,
    val attachments: List<MessageAttachmentResponseDto> = emptyList()
)
