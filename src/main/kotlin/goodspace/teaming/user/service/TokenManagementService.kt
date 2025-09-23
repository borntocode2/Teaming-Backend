package goodspace.teaming.user.service

import goodspace.teaming.authorization.dto.AccessTokenReissueRequestDto
import goodspace.teaming.authorization.dto.AccessTokenResponseDto

interface TokenManagementService {
    fun reissueAccessToken(requestDto: AccessTokenReissueRequestDto): AccessTokenResponseDto

    fun expireRefreshToken(userId: Long)
}
