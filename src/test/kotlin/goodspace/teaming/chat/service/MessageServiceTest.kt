package goodspace.teaming.chat.service

import goodspace.teaming.chat.domain.mapper.ChatMessageResponseMapper
import goodspace.teaming.chat.dto.ChatMessageResponseDto
import goodspace.teaming.chat.dto.ChatSendRequestDto
import goodspace.teaming.chat.event.ChatMessageCreatedEvent
import goodspace.teaming.global.entity.room.*
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.repository.FileRepository
import goodspace.teaming.global.repository.MessageRepository
import goodspace.teaming.global.repository.UserRoomRepository
import io.mockk.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
import org.springframework.test.context.event.RecordApplicationEvents
import java.lang.IllegalArgumentException
import kotlin.math.min

private const val ROOM_ID = 999L
private const val USER_ID = 99L
private const val WRONG_ID = 9999L
private const val RECENT_MESSAGE_LOWER_BOUND = 10
private const val RECENT_MESSAGE_UPPER_BOUND = 50

@RecordApplicationEvents
class MessageServiceTest {
    private val userRoomRepository = mockk<UserRoomRepository>()
    private val messageRepository = mockk<MessageRepository>()
    private val fileRepository = mockk<FileRepository>()
    private val chatMessageResponseMapper = mockk<ChatMessageResponseMapper>()
    private val eventPublisher = mockk<ApplicationEventPublisher>(relaxed = true)

    private val messageService: MessageService = MessageServiceImpl(
        userRoomRepository = userRoomRepository,
        messageRepository = messageRepository,
        fileRepository = fileRepository,
        chatMessageResponseMapper = chatMessageResponseMapper,
        eventPublisher = eventPublisher,
        recentMessageLowerBound = RECENT_MESSAGE_LOWER_BOUND,
        recentMessageUpperBound = RECENT_MESSAGE_UPPER_BOUND
    )

    private var existingMessagesCount: Int = 0

    // 공통 픽스쳐
    private val user = mockk<User>(relaxed = true) { every { id } returns USER_ID }
    private val room = mockk<Room>(relaxed = true) { every { id } returns ROOM_ID }
    private val userRoom = createUserRoom(user, room, PaymentStatus.PAID)

    @BeforeEach
    fun mocking() {
        clearMocks(userRoomRepository, messageRepository, fileRepository, chatMessageResponseMapper, eventPublisher)

        every { userRoomRepository.findByRoomIdAndUserId(any(), any()) } returns null
        every { userRoomRepository.findByRoomIdAndUserId(ROOM_ID, USER_ID) } returns userRoom

        every { fileRepository.findAllById(any<Iterable<Long>>()) } returns emptyList()

        every { messageRepository.findByClientMessageIdAndRoomAndSender(any(), any(), any()) } returns null
        every { messageRepository.save(any<Message>()) } answers { firstArg() }

        // 공용: pageable과 existingMessagesCount를 기반으로 Slice 생성
        fun <T> sliceOf(pageable: Pageable, availableCount: Int, factory: () -> T): Slice<T> {
            val requested = pageable.pageSize
            val contentSize = min(requested, availableCount)
            val content = List(contentSize) { factory() }
            val hasNext = availableCount > contentSize
            return SliceImpl(content, pageable, hasNext)
        }

        // 최신 메시지 페이지 (id desc)
        every { messageRepository.findByRoomOrderByIdDesc(any(), any<PageRequest>()) } answers {
            val pageable = secondArg<PageRequest>()
            sliceOf(pageable, existingMessagesCount) { mockk<Message>(relaxed = true) }
        }

        // 특정 id 미만 페이징 (필요 시 대비)
        every { messageRepository.findByRoomAndIdLessThanOrderByIdDesc(any(), any(), any<PageRequest>()) } answers {
            val pageable = thirdArg<PageRequest>()
            sliceOf(pageable, existingMessagesCount) { mockk<Message>(relaxed = true) }
        }

        every { chatMessageResponseMapper.map(any<Message>()) } returns mockk<ChatMessageResponseDto>(relaxed = true)

        existingMessagesCount = 30
    }

    @Nested
    @DisplayName("saveMessage")
    inner class SaveMessage {
        @Test
        fun `메시지를 저장한다`() {
            // given
            val request = getChatSendRequestDto()

            // when
            messageService.saveMessage(USER_ID, ROOM_ID, request)

            // then
            verify(exactly = 1) { messageRepository.save(any<Message>()) }
        }

        @Test
        fun `결제되지 않았다면 예외가 발생한다`() {
            // given
            val request = getChatSendRequestDto()
            userRoom.paymentStatus = PaymentStatus.NOT_PAID

            // when & then
            assertThatThrownBy { messageService.saveMessage(USER_ID, ROOM_ID, request) }
        }

        @Test
        fun `회원이 해당 방에 소속되어 있지 않다면 예외가 발생한다`() {
            // given
            val request = getChatSendRequestDto()

            // when & then
            assertThatThrownBy { messageService.saveMessage(USER_ID, WRONG_ID, request) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `메시지 저장 이벤트가 발생한다`() {
            // given
            val request = getChatSendRequestDto()

            // when
            messageService.saveMessage(USER_ID, ROOM_ID, request)

            // then
            verify(exactly = 1) { eventPublisher.publishEvent(ofType<ChatMessageCreatedEvent>()) }
        }
    }

    @Nested
    @DisplayName("findMessages")
    inner class FindMessages {
        @ParameterizedTest
        @ValueSource(ints = [20, 30, 40])
        fun `amount만큼 최신 메시지를 조회한다`(requestAmount: Int) {
            // given
            existingMessagesCount = 10_000

            // when
            val result = messageService.findMessages(USER_ID, ROOM_ID, requestAmount)

            // then
            assertThat(result.items.size).isEqualTo(requestAmount)
        }

        @Test
        fun `결제되지 않았다면 예외가 발생한다`() {
            // given
            existingMessagesCount = 10_000
            val requestAmount = 40
            userRoom.paymentStatus = PaymentStatus.NOT_PAID

            // when & then
            assertThatThrownBy { messageService.findMessages(USER_ID, ROOM_ID, requestAmount) }
        }

        @Test
        fun `기존에 존재하는 메시지가 limit보다 작다면, 있는 만큼만 반환한다`() {
            // given
            existingMessagesCount = 7
            val requestAmount = 40

            // when
            val result = messageService.findMessages(USER_ID, ROOM_ID, requestAmount)

            // then
            assertThat(result.items.size).isEqualTo(existingMessagesCount)
            assertThat(result.hasNext).isFalse()
        }

        @ParameterizedTest
        @ValueSource(ints = [1000, 2000, 3000])
        fun `너무 많은 양의 메시지를 요청하면, 상한선까지만 제공한다`(requestAmount: Int) {
            // given
            existingMessagesCount = 10_000

            // when
            val result = messageService.findMessages(USER_ID, ROOM_ID, requestAmount)

            // then
            assertThat(result.items.size).isEqualTo(RECENT_MESSAGE_UPPER_BOUND)
            assertThat(result.hasNext).isTrue()
        }

        @ParameterizedTest
        @ValueSource(ints = [-100, -1, 0, 1])
        fun `너무 적은 양의 메시지를 요청하면, 하한선만큼 제공한다`(requestAmount: Int) {
            // given
            existingMessagesCount = 10_000

            // when
            val result = messageService.findMessages(USER_ID, ROOM_ID, requestAmount)

            // then
            assertThat(result.items.size).isEqualTo(RECENT_MESSAGE_LOWER_BOUND)
            assertThat(result.hasNext).isTrue()
        }
    }

    private fun createUserRoom(user: User, room: Room, paymentStatus: PaymentStatus): UserRoom {
        return UserRoom(
            user = user,
            room = room,
            roomRole = RoomRole.MEMBER,
            paymentStatus = paymentStatus
        )
    }

    private fun getChatSendRequestDto(): ChatSendRequestDto {
        return ChatSendRequestDto(
            content = "nope",
            type = MessageType.TEXT,
            clientMessageId = "client-2",
            attachmentFileIdsInOrder = emptyList()
        )
    }
}
