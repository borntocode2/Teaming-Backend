package goodspace.teaming.chat.controller

import goodspace.teaming.chat.dto.*
import goodspace.teaming.chat.service.MessageService
import goodspace.teaming.chat.service.RoomService
import goodspace.teaming.chat.service.UnreadService
import goodspace.teaming.global.security.getUserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.Principal

private val NO_CONTENT = ResponseEntity.noContent().build<Void>()

@RestController
@RequestMapping("/rooms")
@Tag(
    name = "채팅 API (HTTP)",
    description = "HTTP 프로토콜을 사용하는 채팅 API입니다."
)
class ChatRestController(
    private val unreadService: UnreadService,
    private val messageService: MessageService,
    private val roomService: RoomService
) {
    @PostMapping
    @Operation(
        summary = "방 생성",
        description = "티밍룸을 생성합니다."
    )
    fun createRoom(
        principal: Principal,
        @RequestBody requestDto: RoomCreateRequestDto
    ): ResponseEntity<Void> {
        val userId = principal.getUserId()

        roomService.createRoom(userId, requestDto)

        return NO_CONTENT
    }

    @GetMapping
    @Operation(
        summary = "방 조회",
        description = "해당 사용자가 참여한 모든 티밍룸 정보를 조회합니다."
    )
    fun getRooms(
        principal: Principal
    ): ResponseEntity<List<RoomInfoResponseDto>> {
        val userId = principal.getUserId()

        val response = roomService.getRooms(userId)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/search")
    @Operation(
        summary = "방 검색",
        description = "초대 코드를 통해 티밍룸을 검색합니다."
    )
    fun searchRoom(
        @RequestParam inviteCode: String
    ): ResponseEntity<RoomSearchResponseDto> {
        val response = roomService.searchRoom(inviteCode)

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{roomId}")
    @Operation(
        summary = "방 떠나기",
        description = "티밍룸을 떠납니다. 사전에 결제가 완료되어야 있어야 합니다."
    )
    fun leaveRoom(
        principal: Principal,
        @PathVariable roomId: Long
    ): ResponseEntity<Void> {
        val userId = principal.getUserId()

        roomService.leaveRoom(userId, roomId)

        return NO_CONTENT
    }

    @PatchMapping("/{roomId}/success")
    @Operation(
        summary = "팀플 성공",
        description = "해당 방의 팀플을 종료합니다."
    )
    fun successTeaming(
        principal: Principal,
        @PathVariable roomId: Long
    ): ResponseEntity<Void> {
        val userId = principal.getUserId()

        roomService.setSuccess(userId, roomId)

        return NO_CONTENT
    }

    @PostMapping("/invite")
    @Operation(
        summary = "초대 수락",
        description = "초대 코드를 통해 티밍룸에 들어갑니다."
    )
    fun acceptInvite(
        principal: Principal,
        @RequestBody requestDto: InviteAcceptRequestDto
    ): ResponseEntity<RoomInfoResponseDto> {
        val userId = principal.getUserId()

        val response = roomService.acceptInvite(userId, requestDto)

        return ResponseEntity.ok(response)
    }

    @GetMapping("/unread")
    @Operation(
        summary = "읽지 않은 메시지 개수 조회",
        description = "모든 방의 읽지 않은 메시지 개수를 조회합니다."
    )
    fun getUnreadCounts(principal: Principal): ResponseEntity<List<RoomUnreadCountResponseDto>> {
        val userId = principal.getUserId()

        val response = unreadService.getUnreadCounts(userId)

        return ResponseEntity.ok(response)
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
    ): ResponseEntity<ChatMessagePageResponseDto> {
        val userId = principal.getUserId()

        val response = messageService.findMessages(userId, roomId, limit, cursor)

        return ResponseEntity.ok(response)
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
