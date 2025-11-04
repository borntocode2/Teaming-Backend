package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.global.entity.room.Room
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RoomReadyMapperTest {
    private val roomReadyMapper = RoomReadyMapper()

    @Nested
    inner class `방에 모든 인원이 참여했는지를 확인한다` {
        @Test
        fun `모든 인원이 참여했다면 true를 반환한다`() {
            // given
            val room = mockk<Room>()
            every { room.everyMemberEnteredOrSuccess() } returns true
            every { room.everyMemberPaid() } returns true

            // when
            val result = roomReadyMapper.map(room)

            // then
            assertThat(result.everyMemberEntered).isTrue()
        }

        @Test
        fun `모든 인원이 참여하지 않았다면 false를 반환한다`() {
            // given
            val room = mockk<Room>()
            every { room.everyMemberEnteredOrSuccess() } returns false
            every { room.everyMemberPaid() } returns true

            // when
            val result = roomReadyMapper.map(room)

            // then
            assertThat(result.everyMemberEntered).isFalse()
        }
    }

    @Nested
    inner class `모든 멤버가 결제하였는지를 확인한다` {
        @Test
        fun `모든 인원이 결제했다면 true를 반환한다`() {
            // given
            val room = mockk<Room>()
            every { room.everyMemberPaid() } returns true
            every { room.everyMemberEnteredOrSuccess() } returns true

            // when
            val result = roomReadyMapper.map(room)

            // then
            assertThat(result.everyMemberPaid).isTrue()
        }

        @Test
        fun `모든 인원이 결제하지 않았다면 false를 반환한다`() {
            // given
            val room = mockk<Room>()
            every { room.everyMemberPaid() } returns false
            every { room.everyMemberEnteredOrSuccess() } returns true

            // when
            val result = roomReadyMapper.map(room)

            // then
            assertThat(result.everyMemberPaid).isFalse()
        }
    }
}
