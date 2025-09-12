package goodspace.teaming.chat.controller

import goodspace.teaming.chat.dto.ChatMessagePageResponseDto
import goodspace.teaming.chat.dto.MarkReadRequestDto
import goodspace.teaming.chat.dto.RoomUnreadCountResponseDto
import goodspace.teaming.chat.service.ChatService
import goodspace.teaming.chat.service.UnreadService
import goodspace.teaming.global.security.getUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/rooms")
@Tag(
    name = "채팅 API (HTTP)",
    description = "HTTP 프로토콜을 사용하는 채팅 API들입니다."
)
class ChatRestController(
    private val unreadService: UnreadService,
    private val chatService: ChatService
) {
    @GetMapping("/unread")
    @Operation(
        summary = "읽지 않은 메시지 개수 조회",
        description = "모든 방의 읽지 않은 메시지 개수를 조회합니다."
    )
    fun getUnreadCounts(principal: Principal): List<RoomUnreadCountResponseDto> {
        val userId = principal.getUserId()

        return unreadService.getUnreadCounts(userId)
    }

    @GetMapping("/{roomId}/messages")
    @Operation(
        summary = "메시지 이력 조회",
        description = """
            스크롤링을 통해 과거 메시지를 조회할 때 사용합니다.
            현재까지 조회한 메시지 ID 중 가장 작은 값을 cursor 담으면, 그 이전에 있는 메시지를 limit만큼 반환합니다.
            처음 조회 시에는 cursor 값을 null로 주면 됩니다.
            limit는 1 ~ 200 사이의 값입니다.
        """
    )
    fun getMessages(
        principal: Principal,
        @PathVariable roomId: Long,
        @RequestParam(defaultValue = "50") limit: Int,
        @RequestParam(required = false) cursor: Long?
    ): ChatMessagePageResponseDto {
        val userId = principal.getUserId()

        return chatService.findMessages(userId, roomId, limit.coerceIn(1, 200), cursor)
    }

    @PostMapping("/{roomId}/read")
    @Operation(
        summary = "읽음 경계 업데이트",
        description = "읽지 않은 메시지 개수를 최신화합니다. lastReadMessageId 값이 null이라면 가장 최신 메시지까지 읽은 것으로 간주합니다."
    )
    fun markRead(
        principal: Principal,
        @PathVariable roomId: Long,
        @RequestBody request: MarkReadRequestDto
    ): RoomUnreadCountResponseDto {
        val userId = principal.getUserId()

        return unreadService.markRead(userId, roomId, request.lastReadMessageId)
    }
}
