package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.ChatMessageResponseDto
import goodspace.teaming.chat.dto.MessageAttachmentResponseDto
import goodspace.teaming.chat.dto.SenderSummaryResponseDto
import goodspace.teaming.fixture.FileFixture
import goodspace.teaming.fixture.RoomFixture
import goodspace.teaming.fixture.TeamingUserFixture
import goodspace.teaming.global.entity.file.Attachment
import goodspace.teaming.global.entity.file.File
import goodspace.teaming.global.entity.room.Message
import goodspace.teaming.global.entity.room.MessageType
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.global.entity.user.User
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.time.Instant

private const val DEFAULT_MESSAGE_ID = 1001L
private const val DEFAULT_ROOM_ID = 2002L
private const val DEFAULT_USER_ID = 3003L
private const val DEFAULT_CLIENT_MESSAGE_ID = "client-123"
private const val DEFAULT_CONTENT = "hello world"
private val DEFAULT_CREATED_AT = Instant.now()

class ChatMessageResponseMapperTest {
    private val attachmentMapper = mockk<AttachmentMapper>()
    private val senderSummaryMapper = mockk<SenderSummaryMapper>()
    private val mapper = ChatMessageResponseMapper(attachmentMapper, senderSummaryMapper)

    @BeforeEach
    fun setupMocks() {
        every { senderSummaryMapper.map(any(), any()) } returns SenderSummaryResponseDto(
            id = DEFAULT_USER_ID,
            name = "Sender Name",
            avatarUrl = "https://cdn/avatar.png"
        )
    }

    @Nested
    @DisplayName("기본 필드 매핑")
    inner class BasicFieldMapping {
        @Test
        fun `메시지의 기본 필드를 그대로 매핑한다`() {
            // given
            val room = createRoom()
            val user = createUser()
            val message = Message(
                sender = user,
                room = room,
                content = DEFAULT_CONTENT,
                type = MessageType.TEXT,
                clientMessageId = DEFAULT_CLIENT_MESSAGE_ID
            )
            message.createdAt = DEFAULT_CREATED_AT
            ReflectionTestUtils.setField(message, "id", DEFAULT_MESSAGE_ID)

            // when
            val dto: ChatMessageResponseDto = mapper.map(message)

            // then
            assertThat(dto.messageId).isEqualTo(DEFAULT_MESSAGE_ID)
            assertThat(dto.roomId).isEqualTo(DEFAULT_ROOM_ID)
            assertThat(dto.clientMessageId).isEqualTo(DEFAULT_CLIENT_MESSAGE_ID)
            assertThat(dto.type).isEqualTo(MessageType.TEXT)
            assertThat(dto.content).isEqualTo(DEFAULT_CONTENT)
            assertThat(dto.createdAt).isEqualTo(DEFAULT_CREATED_AT)
        }
    }

    @Nested
    @DisplayName("첨부 매핑")
    inner class AttachmentMapping {
        @Test
        fun `첨부가 없으면 빈 리스트를 반환한다`() {
            // given
            val room = createRoom()
            val user = createUser()
            val message = Message(
                sender = user,
                room = room,
                content = DEFAULT_CONTENT,
                type = MessageType.TEXT,
                clientMessageId = DEFAULT_CLIENT_MESSAGE_ID
            )
            message.createdAt = DEFAULT_CREATED_AT
            ReflectionTestUtils.setField(message, "id", DEFAULT_MESSAGE_ID)

            // when
            val dto = mapper.map(message)

            // then
            assertThat(dto.attachments).isEmpty()
        }

        @Test
        fun `매핑 후에도 첨부 파일의 순서가 유지된다`() {
            // given
            val room = createRoom()
            val user = createUser()
            val message = Message(
                sender = user,
                room = room,
                content = null,
                type = MessageType.FILE,
                clientMessageId = DEFAULT_CLIENT_MESSAGE_ID
            )
            message.createdAt = DEFAULT_CREATED_AT
            ReflectionTestUtils.setField(message, "id", DEFAULT_MESSAGE_ID)

            val firstAttachment = createAttachment(
                file = FileFixture.IMAGE.getInstanceWith(room, user),
                message = message,
                sortOrder = 0
            )
            val secondAttachment = createAttachment(
                file = FileFixture.VIDEO.getInstanceWith(room, user),
                message = message,
                sortOrder = 1
            )
            message.attachments.add(firstAttachment)
            message.attachments.add(secondAttachment)

            val firstMapped = createAttachmentDto(
                fileId = 10L,
                sortOrder = 0,
                fixture = FileFixture.IMAGE,
                room = room,
                user = user,
                previewUrl = "p1",
                thumbnailUrl = "t1",
                downloadUrl = "d1",
            )
            val secondMapped = createAttachmentDto(
                fileId = 20L,
                sortOrder = 1,
                fixture = FileFixture.VIDEO,
                room = room,
                user = user,
                previewUrl = "p2",
                thumbnailUrl = "t2",
                downloadUrl = "d2",
            )

            every { attachmentMapper.map(firstAttachment) } returns firstMapped
            every { attachmentMapper.map(secondAttachment) } returns secondMapped

            // when
            val dto = mapper.map(message)

            // then
            assertThat(dto.attachments).containsExactly(firstMapped, secondMapped)
        }
    }

    private fun createRoom(): Room {
        return RoomFixture.DEFAULT.getInstance().also {
            ReflectionTestUtils.setField(it, "id", DEFAULT_ROOM_ID)
        }
    }

    private fun createUser(): TeamingUser {
        val user = TeamingUserFixture.A.getInstance().also {
            ReflectionTestUtils.setField(it, "id", DEFAULT_USER_ID)
        }
        return user
    }

    private fun createAttachment(
        file: File,
        message: Message,
        sortOrder: Int
    ): Attachment {
        val fileId = getRandomId()
        ReflectionTestUtils.setField(file, "id", fileId)

        val attachment = Attachment(
            message = message,
            file = file,
            sortOrder = sortOrder
        )
        val attachmentId = getRandomId()
        ReflectionTestUtils.setField(attachment, "id", attachmentId)

        return attachment
    }

    private fun createAttachmentDto(
        fileId: Long,
        sortOrder: Int,
        fixture: FileFixture,
        room: Room,
        user: User,
        previewUrl: String,
        thumbnailUrl: String,
        downloadUrl: String,
    ): MessageAttachmentResponseDto {
        val file = fixture.getInstanceWith(room, user)

        return MessageAttachmentResponseDto(
            fileId = fileId,
            sortOrder = sortOrder,
            name = file.name,
            type = file.type,
            mimeType = file.mimeType,
            byteSize = file.byteSize,
            width = file.width,
            height = file.height,
            durationMs = file.durationMs,
            previewUrl = previewUrl,
            thumbnailUrl = thumbnailUrl,
            downloadUrl = downloadUrl,
            antiVirusScanStatus = file.antiVirusScanStatus,
            transcodeStatus = file.transcodeStatus,
            ready = true
        )
    }

    private fun getRandomId(): Long {
        return (1000..9999).random().toLong()
    }
}
