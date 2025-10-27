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
            // given: 5명까지 참여 가능한 방에 5명이 참여한 상태
            val room = mockk<Room>()
            every { room.currentMemberCount() } returns 5
            every { room.memberCount } returns 5

            // when
            val result = roomReadyMapper.map(room)

            // then
            assertThat(result.everyMemberEntered).isTrue()
        }

        @Test
        fun `모든 인원이 참여하지 않았다면 false를 반환한다`() {
            // given: 5명까지 참여 가능한 방에 4명이 참여한 상태
            val room = mockk<Room>()
            every { room.currentMemberCount() } returns 4
            every { room.memberCount } returns 5

            // when
            val result = roomReadyMapper.map(room)

            // then
            assertThat(result.everyMemberEntered).isFalse()
        }
    }

}
