package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.ChatMessageResponseDto
import goodspace.teaming.global.entity.room.Message
import org.springframework.stereotype.Component

@Component
class ChatMessageResponseMapper(
    private val attachmentMapper: AttachmentMapper,
    private val senderSummaryMapper: SenderSummaryMapper
) {
    fun map(message: Message): ChatMessageResponseDto {
        return ChatMessageResponseDto(
            messageId = message.id!!,
            roomId = message.room.id!!,
            clientMessageId = message.clientMessageId,
            type = message.type,
            content = message.content,
            createdAt = message.createdAt!!,
            sender = senderSummaryMapper.map(message.sender),
            attachments = message.attachments.map { attachment ->
                attachmentMapper.map(attachment)
            }
        )
    }
}
