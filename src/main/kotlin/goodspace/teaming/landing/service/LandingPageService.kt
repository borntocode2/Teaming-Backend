package goodspace.teaming.landing.service

import goodspace.teaming.landing.dto.StatisticResponseDto

interface LandingPageService {
    fun getStatistics(): StatisticResponseDto
}
