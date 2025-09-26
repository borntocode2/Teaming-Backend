package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.chat.dto.RoomMemberResponseDto
import goodspace.teaming.file.domain.CdnStorageUrlProvider
import goodspace.teaming.global.entity.room.RoomRole
import goodspace.teaming.global.entity.room.UserRoom
import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.util.createRoom
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

private const val USER_NAME = "USER_NAME"
private const val USER_AVATAR_KEY = "USER_AVATAR_KEY"
private const val USER_AVATAR_VERSION = 1
private const val USER_EMAIL = "USER@EMAIL"
private const val USER_PASSWORD = "USER_PASSWORD"
private const val USER_ID = 10L
private const val USER_ROOM_ID = 30L
private val ROOM_ROLE = RoomRole.MEMBER
private const val LAST_READ_MESSAGE_ID = 40L
private const val AVATAR_URL = "AVATAR_URL"

class RoomMemberMapperTest {
    private val storageUrlProvider = mockk<CdnStorageUrlProvider>()
    private val roomMemberMapper = RoomMemberMapper(storageUrlProvider)

    @Test
    @DisplayName("엔티티를 DTO로 올바르게 매핑한다")
    fun `map should convert UserRoom to RoomMemberResponseDto`() {
        // given
        val user = TeamingUser(
            name = USER_NAME,
            email = USER_EMAIL,
            password = USER_PASSWORD,
            avatarKey = USER_AVATAR_KEY,
            avatarVersion = USER_AVATAR_VERSION,
        )
        ReflectionTestUtils.setField(user, "id", USER_ID)

        val room = createRoom()

        val userRoom = UserRoom(
            user = user,
            room = room,
            roomRole = RoomRole.MEMBER,
            lastReadMessageId = LAST_READ_MESSAGE_ID
        )
        ReflectionTestUtils.setField(userRoom, "id", USER_ROOM_ID)

        every { storageUrlProvider.publicUrl(user.avatarKey, user.avatarVersion) } returns AVATAR_URL

        // when
        val result: RoomMemberResponseDto = roomMemberMapper.map(userRoom)

        // then
        assertThat(result.memberId).isEqualTo(USER_ID)
        assertThat(result.name).isEqualTo(USER_NAME)
        assertThat(result.avatarUrl).isEqualTo(AVATAR_URL)
        assertThat(result.avatarVersion).isEqualTo(USER_AVATAR_VERSION)
        assertThat(result.roomRole).isEqualTo(ROOM_ROLE)
        assertThat(result.lastReadMessageId).isEqualTo(LAST_READ_MESSAGE_ID)
        assertThat(result.avatarUrl).isEqualTo(AVATAR_URL)
    }
}
