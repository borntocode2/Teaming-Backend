package goodspace.teaming.chat.service

import goodspace.teaming.chat.dto.ChatMessagePageResponseDto
import goodspace.teaming.chat.dto.ChatMessageResponseDto
import goodspace.teaming.chat.dto.ChatSendRequestDto
import goodspace.teaming.chat.dto.MessageAttachmentResponseDto

interface MessageService {
    fun saveMessage(
        userId: Long,
        roomId: Long,
        requestDto: ChatSendRequestDto
    ): ChatMessageResponseDto

    fun findMessages(
        userId: Long,
        roomId: Long,
        amount: Int = 50,
        beforeMessageId: Long? = null
    ): ChatMessagePageResponseDto

    fun getMessageAttachment(
        userId: Long,
        roomId: Long
    ): List<MessageAttachmentResponseDto>
}
