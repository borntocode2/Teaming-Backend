package goodspace.teaming.util

import goodspace.teaming.fixture.*
import goodspace.teaming.global.entity.email.EmailVerification
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.room.RoomType
import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.global.entity.user.UserType
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime

fun createUser(
    email: String = USER_EMAIL,
    name: String = USER_NAME,
    password: String = USER_PASSWORD,
    type: UserType = UserType.TEAMING,
    id: Long = USER_ID
): TeamingUser {
    val user = TeamingUser(
        email = email,
        name = name,
        password = password
    )
    ReflectionTestUtils.setField(user, "type", type)
    ReflectionTestUtils.setField(user, "id", id)

    return user
}

fun createEmailVerification(
    email: String = EMAIL_VERIFICATION_EMAIL,
    verified: Boolean = true,
    code: String = EMAIL_VERIFICATION_CODE,
    expiresAt: LocalDateTime = EMAIL_VERIFICATION_EXPIRES_AT,
    id: Long = EMAIL_VERIFICATION_ID
): EmailVerification {
    val emailVerification = EmailVerification(
        email = email,
        verified = verified,
        code = code,
        expiresAt = expiresAt
    )
    ReflectionTestUtils.setField(emailVerification, "id", id)

    return emailVerification
}

fun createRoom(
    title: String = ROOM_TITLE,
    imageKey: String? = ROOM_IMAGE_KEY,
    imageVersion: Int? = ROOM_IMAGE_VERSION,
    type: RoomType = ROOM_TYPE,
    inviteCode: String? = ROOM_INVITE_CODE,
    memberCount: Int = ROOM_MEMBER_COUNT,
    id: Long = ROOM_ID,
    success: Boolean = false
): Room {
    val room = Room(
        title = title,
        imageKey = imageKey,
        imageVersion = imageVersion,
        type = type,
        inviteCode = inviteCode,
        memberCount = memberCount
    )
    ReflectionTestUtils.setField(room, "id", id)
    ReflectionTestUtils.setField(room, "success", success)

    return room
}
