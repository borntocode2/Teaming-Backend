package goodspace.teaming.chat.controller

import goodspace.teaming.chat.dto.ChatMessageResponseDto
import goodspace.teaming.chat.dto.ChatSendRequestDto
import goodspace.teaming.chat.service.ChatService
import goodspace.teaming.global.security.getUserId
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.stereotype.Controller
import java.security.Principal

private const val PREFIX = "rooms"

@Controller
@MessageMapping(PREFIX)
@Tag(
    name = "채팅 API",
    description = "AsyncAPI 문서 링크 첨부 예정"
)
class ChatController(
    private val chatService: ChatService,
    private val messaging: SimpMessagingTemplate
) {
    @MessageMapping("/{roomId}/send")
    fun sendMessage(
        @DestinationVariable roomId: Long,
        @Payload requestDto: ChatSendRequestDto,
        principal: Principal
    ) {
        val senderId = principal.getUserId()

        val savedMessage = chatService.saveMessage(senderId, roomId, requestDto)

        messaging.convertAndSend("/topic/$PREFIX/$roomId", savedMessage)
    }

    @SubscribeMapping("/{roomId}/initial")
    fun initialHistory(
        @DestinationVariable roomId: Long,
        principal: Principal
    ): List<ChatMessageResponseDto> {
        val userId = principal.getUserId()

        return chatService.findRecentMessages(userId, roomId)
    }

    /**
     * 예외 발생시 개인 큐로 전송
     */
    @MessageExceptionHandler
    fun handleException(
        exception: Exception,
        principal: Principal?
    ) {
        val user = principal?.name ?: return

        messaging.convertAndSendToUser(
            user,
            "/queue/errors",
            exception.message ?: "Unknown Error"
        )
    }
}
