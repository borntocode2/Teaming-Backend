package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.global.entity.room.Message
import goodspace.teaming.global.entity.room.MessageType
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.time.ZoneId

private const val CONTENT = "hello world"
private const val CLIENT_MESSAGE_ID = "clientMessageId"
private const val ID = 6L
private val TYPE = MessageType.TEXT
private val CREATED_AT = LocalDateTime.now()

class LastMessagePreviewMapperTest {
    private val senderSummaryMapper = mockk<SenderSummaryMapper>(relaxed = true)
    private val lastMessagePreviewMapper = LastMessagePreviewMapper(senderSummaryMapper, ZoneId.systemDefault())

    @Test
    fun `메시지의 속성을 기반으로 DTO를 생성해 반환한다`() {
        // given
        val message = Message(
            sender = mockk(),
            room = mockk(),
            content = CONTENT,
            type = TYPE,
            clientMessageId = CLIENT_MESSAGE_ID
        )
        ReflectionTestUtils.setField(message, "id", ID)
        ReflectionTestUtils.setField(message, "createdAt", CREATED_AT)

        // when
        val dto = lastMessagePreviewMapper.map(message)

        // then
        assertThat(dto.content).isEqualTo(CONTENT)
        assertThat(dto.type).isEqualTo(TYPE)
        assertThat(dto.createdAt).isEqualTo(CREATED_AT.atZone(ZoneId.systemDefault()).toInstant())
    }

    @Test
    fun `메시지의 id 값이 null이면 예외가 발생한다`() {
        // given
        val message = Message(
            sender = mockk(),
            room = mockk(),
            content = CONTENT,
            type = TYPE,
            clientMessageId = CLIENT_MESSAGE_ID
        )

        // when & then
        assertThatThrownBy { lastMessagePreviewMapper.map(message) }
            .isInstanceOf(NullPointerException::class.java)
    }
}
