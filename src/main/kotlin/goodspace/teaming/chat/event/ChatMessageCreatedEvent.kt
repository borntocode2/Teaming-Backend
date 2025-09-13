package goodspace.teaming.chat.event

import goodspace.teaming.chat.dto.ChatMessageResponseDto

data class ChatMessageCreatedEvent(
    val roomId: Long,
    val senderId: Long,
    val payload: ChatMessageResponseDto
)
