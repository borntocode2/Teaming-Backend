package goodspace.teaming.authorization.service

import goodspace.teaming.authorization.dto.AccessTokenReissueRequestDto
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.security.TokenProvider
import goodspace.teaming.global.security.TokenType
import goodspace.teaming.util.createUser
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*

private const val REFRESH_TOKEN = "refreshToken"
private const val DIFFERENT_REFRESH_TOKEN = "differentRefreshToken"
private const val ACCESS_TOKEN = "accessToken"

class TokenManagementServiceTest {
    private val userRepository = mockk<UserRepository>()
    private val tokenProvider = mockk<TokenProvider>()

    private val tokenManagementService = TokenManagementService(
        userRepository = userRepository,
        tokenProvider = tokenProvider
    )

    @Nested
    @DisplayName("reissueAccessToken")
    inner class ReissueAccessToken {
        @Test
        fun `엑세스 토큰을 재발급한다`() {
            // given
            val user = createUser()
            user.token = REFRESH_TOKEN

            val requestDto = AccessTokenReissueRequestDto(REFRESH_TOKEN)

            every { tokenProvider.getIdFromToken(REFRESH_TOKEN) } returns user.id!!
            every { userRepository.findById(user.id!!) } returns Optional.of(user)
            every { tokenProvider.validateToken(REFRESH_TOKEN, TokenType.REFRESH) } returns true
            every { tokenProvider.createToken(user.id!!, TokenType.ACCESS, any()) } returns ACCESS_TOKEN

            // when
            val response = tokenManagementService.reissueAccessToken(requestDto)

            // then
            assertThat(response.accessToken).isEqualTo(ACCESS_TOKEN)
        }

        @Test
        fun `리프레쉬 토큰이 부적절하면 예외를 던진다`() {
            // given
            val user = createUser()
            user.token = REFRESH_TOKEN

            val requestDto = AccessTokenReissueRequestDto(REFRESH_TOKEN)

            every { tokenProvider.getIdFromToken(REFRESH_TOKEN) } returns user.id!!
            every { userRepository.findById(user.id!!) } returns Optional.of(user)
            every { tokenProvider.validateToken(REFRESH_TOKEN, TokenType.REFRESH) } returns false

            // when & then
            assertThatThrownBy { tokenManagementService.reissueAccessToken(requestDto) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("부적절한 토큰입니다.")
        }

        @Test
        fun `회원의 리프레쉬 토큰과 일치하지 않으면 예외를 던진다`() {
            val user = createUser()
            user.token = REFRESH_TOKEN

            val requestDto = AccessTokenReissueRequestDto(DIFFERENT_REFRESH_TOKEN)

            every { tokenProvider.getIdFromToken(DIFFERENT_REFRESH_TOKEN) } returns user.id!!
            every { userRepository.findById(user.id!!) } returns Optional.of(user)
            every { tokenProvider.validateToken(DIFFERENT_REFRESH_TOKEN, TokenType.REFRESH) } returns true

            // when & then
            assertThatThrownBy { tokenManagementService.reissueAccessToken(requestDto) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("만료된 리프레쉬 토큰입니다.")
        }
    }
}
