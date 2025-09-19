package goodspace.teaming.landing.service

import goodspace.teaming.global.repository.RoomRepository
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.landing.dto.StatisticResponseDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LandingPageServiceImpl(
    private val userRepository: UserRepository,
    private val roomRepository: RoomRepository
) : LandingPageService {
    @Transactional
    override fun getStatistics(): StatisticResponseDto {
        return StatisticResponseDto(
            totalUserCount = userRepository.count(),
            totalTeamCount = roomRepository.count(),
            completeTeamCount = roomRepository.countBySuccessTrue()
        )
    }
}
