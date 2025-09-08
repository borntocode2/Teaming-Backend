package goodspace.teaming.chat.service

import goodspace.teaming.global.repository.UserRoomRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private const val ROOM_ID = 10L
private const val USER_ID = 5L
private const val WRONG_ID = 99L

class RoomAccessServiceTest {
    private val userRoomRepository = mockk<UserRoomRepository>()
    private val roomAccessService: RoomAccessAuthorizer = RoomAccessService(userRoomRepository)

    @BeforeEach
    fun mocking() {
        every { userRoomRepository.existsByRoomIdAndUserId(any(), any()) } returns false
        every { userRoomRepository.existsByRoomIdAndUserId(ROOM_ID, USER_ID) } returns true
    }

    @Nested
    @DisplayName("assertMemberOf")
    inner class AssertMemberOf {
        @Test
        fun `회원이 해당 티밍룸에 참여하고 있지 않다면 예외를 던진다`() {
            assertThatThrownBy { roomAccessService.assertMemberOf(WRONG_ID, USER_ID) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }

        @Test
        fun `회원이 해당 티밍룸에 참여하고 있다면 예외를 던지지 않는다`() {
            assertThatCode { roomAccessService.assertMemberOf(ROOM_ID, USER_ID) }
                .doesNotThrowAnyException()
        }
    }
}
