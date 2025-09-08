package goodspace.teaming.chat.service

import goodspace.teaming.chat.dto.ChatMessageResponseDto
import goodspace.teaming.chat.dto.ChatSendRequestDto

interface ChatService {
    fun saveMessage(
        userId: Long,
        roomId: Long,
        requestDto: ChatSendRequestDto
    ): ChatMessageResponseDto

    fun findRecentMessages(
        userId: Long,
        roomId: Long,
        amount: Int = 50
    ): List<ChatMessageResponseDto>
}
