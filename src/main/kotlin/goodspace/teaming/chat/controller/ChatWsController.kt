package goodspace.teaming.chat.domain.controller

import goodspace.teaming.chat.dto.ChatSendRequestDto
import goodspace.teaming.chat.service.MessageService
import goodspace.teaming.global.security.userId
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageExceptionHandler
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
@MessageMapping("/rooms")
@Tag(
    name = "채팅 API (웹소캣)",
    description = "AsyncAPI 문서 링크 첨부 예정"
)
class ChatWsController(
    private val messageService: MessageService,
    private val messaging: SimpMessagingTemplate
) {
    /**
     * 메시지 생성
     */
    @MessageMapping("/{roomId}/send")
    fun sendMessage(
        @DestinationVariable roomId: Long,
        @Payload requestDto: ChatSendRequestDto,
        principal: Principal
    ) {
        val senderId = principal.userId

        messageService.saveMessage(senderId, roomId, requestDto)
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
