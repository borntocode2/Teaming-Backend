package goodspace.teaming.chat.domain.mapper

import goodspace.teaming.fixture.FileFixture
import goodspace.teaming.fixture.TeamingUserFixture
import goodspace.teaming.global.entity.file.*
import goodspace.teaming.global.entity.room.Message
import goodspace.teaming.global.entity.room.MessageType
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.file.domain.StorageUrlProvider
import goodspace.teaming.util.createRoom
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.test.util.ReflectionTestUtils

private const val DEFAULT_PUBLIC_URL = "u"
private const val DEFAULT_SORT_ORDER = 0
private const val DEFAULT_DOWNLOAD_URL = "d"
private const val DEFAULT_CLIENT_ID = "123123"
private const val DEFAULT_REFLECTION_ID = 99L
private const val DEFAULT_STORAGE_KEY = "storageKey"
private const val DEFAULT_THUMBNAIL_KEY = "thumbnailKey"
private const val THUMBNAIL_SIZE = 256

class AttachmentMapperTest {
    private val urlProvider = mockk<StorageUrlProvider>()
    private val mapper = AttachmentMapper(urlProvider, THUMBNAIL_SIZE)

    @BeforeEach
    fun mocking() {
        every { urlProvider.publicUrl(any(), any(), any()) } returns DEFAULT_PUBLIC_URL
        every { urlProvider.downloadUrl(any(), any(), any()) } returns DEFAULT_DOWNLOAD_URL
    }

    @Test
    fun `UrlProvider에게 URL을 제공받는다`() {
        // given
        val user = createUser()
        val file = getFileFromFixture(FileFixture.IMAGE, user)
            .apply { storageKey = DEFAULT_STORAGE_KEY }
            .apply { thumbnailKey = DEFAULT_THUMBNAIL_KEY }
        val attachment = getTestAttachmentOf(file, user)

        // when
        mapper.map(attachment)

        // then
        io.mockk.verify { urlProvider.publicUrl(file.storageKey, null, null) }
        io.mockk.verify { urlProvider.publicUrl(file.thumbnailKey, null, THUMBNAIL_SIZE) }
        io.mockk.verify { urlProvider.downloadUrl(file.storageKey, file.name, null) }

        io.mockk.confirmVerified(urlProvider)
    }

    @Test
    fun `전달한 정렬 순서를 유지한다`() {
        // given
        val user = createUser()
        val file = getFileFromFixture(FileFixture.IMAGE, user)
        val attachment = getTestAttachmentOf(file, user, DEFAULT_SORT_ORDER)

        // when
        val dto = mapper.map(attachment)

        // then
        assertThat(dto.sortOrder).isEqualTo(DEFAULT_SORT_ORDER)
    }

    @Test
    fun `이미지 파일은 안티바이러스를 통과했다면 ready를 true로 한다`() {
        // given
        val user = createUser()
        val file = getFileFromFixture(FileFixture.IMAGE, user)
        val attachment = getTestAttachmentOf(file, user)

        // when
        val dto = mapper.map(attachment)

        // then
        assertThat(dto.ready).isTrue()
    }

    @Test
    fun `비디오 파일은 안티바이러스를 통과하더라도 트랜스코딩이 완료되지 않았다면 ready를 false로 한다`() {
        // given
        val user = createUser()
        val file = getFileFromFixture(FileFixture.VIDEO, user)
            .apply { antiVirusScanStatus = AntiVirusScanStatus.PASSED }
            .apply { transcodeStatus = TranscodeStatus.PENDING }
        val attachment = getTestAttachmentOf(file, user)

        // when
        val dto = mapper.map(attachment)

        // then
        assertThat(dto.ready).isFalse()
    }

    @ParameterizedTest
    @EnumSource(FileType::class)
    fun `안티 바이러스가 실패했다면 항상 ready를 false로 한다`(fileType: FileType) {
        // given
        val user = createUser()
        val file = getFileFromFixture(FileFixture.IMAGE, user)
            .apply { type = fileType }
            .apply { antiVirusScanStatus = AntiVirusScanStatus.FAILED }
        val attachment = getTestAttachmentOf(file, user)

        // when
        val dto = mapper.map(attachment)

        // then
        assertThat(dto.ready).isFalse()
    }

    @Test
    fun `썸네일 키가 없으면 thumbnailUrl 값은 null이 된다`() {
        // given
        val user = createUser()
        val file = getFileFromFixture(FileFixture.IMAGE, user)
            .apply { thumbnailKey = null }
        val attachment = getTestAttachmentOf(file, user)

        // when
        val dto = mapper.map(attachment)

        // then
        assertThat(dto.thumbnailUrl).isNull()
    }

    private fun createUser(): User {
        val user = TeamingUserFixture.A.getInstance()
        ReflectionTestUtils.setField(user, "id", goodspace.teaming.fixture.USER_ID)

        return user
    }

    private fun getFileFromFixture(fixture: FileFixture, user: User): File {
        val room = createRoom()

        val file = fixture.getInstanceWith(room, user)
        ReflectionTestUtils.setField(file, "id", DEFAULT_REFLECTION_ID)

        return file
    }

    private fun getTestAttachmentOf(
        file: File,
        sender: User,
        sortOrder: Int = 0
    ): Attachment {
        val message = Message(
            sender = sender,
            room = file.room,
            content = null,
            type = MessageType.FILE,
            clientMessageId = DEFAULT_CLIENT_ID,
        )
        ReflectionTestUtils.setField(message, "id", DEFAULT_REFLECTION_ID)

        val attachment = Attachment(
            message = message,
            file = file,
            sortOrder = sortOrder
        )
        ReflectionTestUtils.setField(attachment, "id", DEFAULT_REFLECTION_ID)

        return attachment
    }
}
