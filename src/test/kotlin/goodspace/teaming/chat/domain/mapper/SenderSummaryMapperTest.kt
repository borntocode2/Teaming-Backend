package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.fixture.TeamingUserFixture
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.storage.StorageUrlProvider
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

private const val DEFAULT_PUBLIC_URL = "https://cdn/avatar.png"
private const val DEFAULT_USER_ID = 42L
private const val DEFAULT_USER_NAME = "Teaming User"
private const val DEFAULT_AVATAR_KEY = "avatars/42.png"
private const val DEFAULT_AVATAR_VERSION = 7
private const val DEFAULT_SIZE = 64
private const val CUSTOM_SIZE = 128

class SenderSummaryMapperTest {
    private val urlProvider = mockk<StorageUrlProvider>()
    private val mapper = SenderSummaryMapper(urlProvider)

    @BeforeEach
    fun setupMocks() {
        every { urlProvider.publicUrl(any(), any(), any()) } answers {
            val keyArgument = firstArg<String?>()
            if (keyArgument.isNullOrBlank()) null else DEFAULT_PUBLIC_URL
        }
    }

    @Nested
    @DisplayName("URL 위임 및 파라미터 전달")
    inner class UrlDelegation {
        @Test
        fun `UrlProvider를 통해 avatarUrl을 담는다`() {
            // given
            val user = createUser(
                id = DEFAULT_USER_ID,
                name = DEFAULT_USER_NAME,
                avatarKey = DEFAULT_AVATAR_KEY,
                avatarVersion = DEFAULT_AVATAR_VERSION
            )

            // when
            val result = mapper.map(user, DEFAULT_SIZE)

            // then
            assertThat(result.id).isEqualTo(DEFAULT_USER_ID)
            assertThat(result.name).isEqualTo(DEFAULT_USER_NAME)
            assertThat(result.avatarUrl).isEqualTo(DEFAULT_PUBLIC_URL)

            verify { urlProvider.publicUrl(DEFAULT_AVATAR_KEY, DEFAULT_AVATAR_VERSION, DEFAULT_SIZE) }
            confirmVerified(urlProvider)
        }

        @Test
        fun `크기를 지정하면 지정한 값으로 URL을 요청한다`() {
            // given
            val user = createUser(
                id = DEFAULT_USER_ID,
                name = DEFAULT_USER_NAME,
                avatarKey = DEFAULT_AVATAR_KEY,
                avatarVersion = DEFAULT_AVATAR_VERSION
            )

            // when
            val result = mapper.map(user, size = CUSTOM_SIZE)

            // then
            assertThat(result.avatarUrl).isEqualTo(DEFAULT_PUBLIC_URL)
            verify { urlProvider.publicUrl(DEFAULT_AVATAR_KEY, DEFAULT_AVATAR_VERSION, CUSTOM_SIZE) }
            confirmVerified(urlProvider)
        }
    }

    @Nested
    @DisplayName("키가 없을 때 동작")
    inner class NullKeyBehavior {

        @Test
        fun `아바타 키가 없으면 avatarUrl 값도 null이 된다`() {
            // given
            val user = createUser(
                id = DEFAULT_USER_ID,
                name = DEFAULT_USER_NAME,
                avatarKey = null
            )

            // when
            val result = mapper.map(user)

            // then
            assertThat(result.avatarUrl).isNull()
            verify { urlProvider.publicUrl(null, null, DEFAULT_SIZE) }
            confirmVerified(urlProvider)
        }
    }

    private fun createUser(
        id: Long,
        name: String,
        avatarKey: String?,
        avatarVersion: Int = 0
    ): User {
        val user = TeamingUserFixture.A.getInstance()
        user.name = name
        user.avatarKey = avatarKey
        user.avatarVersion = avatarVersion

        ReflectionTestUtils.setField(user, "id", id)
        return user
    }
}
