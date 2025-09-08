package goodspace.teaming.chat.dto

import goodspace.teaming.global.entity.room.MessageType

data class ChatSendRequestDto(
    val clientMessageId: String,
    val content: String? = null,
    val type: MessageType = MessageType.TEXT,
    val attachmentFileIdsInOrder: List<Long> = emptyList()
)
