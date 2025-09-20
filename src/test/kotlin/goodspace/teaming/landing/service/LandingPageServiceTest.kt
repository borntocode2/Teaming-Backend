package goodspace.teaming.landing.service

import goodspace.teaming.global.repository.RoomRepository
import goodspace.teaming.global.repository.UserRepository
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private const val TOTAL_USER_COUNT = 1000L
private const val TOTAL_TEAM_COUNT = 100L
private const val COMPLETE_TEAM_COUNT = 50L

class LandingPageServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val roomRepository = mockk<RoomRepository>()
    private val landingPageService = LandingPageServiceImpl(
        userRepository = userRepository,
        roomRepository =  roomRepository
    )

    @Test
    fun `서비스 통계 정보를 제공한다`() {
        // given
        every { userRepository.count() } returns TOTAL_USER_COUNT
        every { roomRepository.count() } returns TOTAL_TEAM_COUNT
        every { roomRepository.countBySuccessTrue() } returns COMPLETE_TEAM_COUNT

        // when
        val result = landingPageService.getStatistics()

        // then
        assertThat(result.totalUserCount).isEqualTo(TOTAL_USER_COUNT)
        assertThat(result.totalTeamCount).isEqualTo(TOTAL_TEAM_COUNT)
        assertThat(result.completeTeamCount).isEqualTo(COMPLETE_TEAM_COUNT)
    }
}
