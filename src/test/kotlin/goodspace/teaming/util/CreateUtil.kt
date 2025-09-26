package goodspace.teaming.util

import goodspace.teaming.fixture.*
import goodspace.teaming.global.entity.aissgnment.Assignment
import goodspace.teaming.global.entity.aissgnment.AssignmentStatus
import goodspace.teaming.global.entity.email.EmailVerification
import goodspace.teaming.global.entity.room.*
import goodspace.teaming.global.entity.user.TeamingUser
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.entity.user.UserType
import org.springframework.test.util.ReflectionTestUtils
import java.time.Instant
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
    description: String = ROOM_DESCRIPTION,
    imageKey: String? = ROOM_IMAGE_KEY,
    imageVersion: Int = ROOM_IMAGE_VERSION,
    type: RoomType = ROOM_TYPE,
    inviteCode: String? = ROOM_INVITE_CODE,
    memberCount: Int = ROOM_MEMBER_COUNT,
    id: Long = ROOM_ID,
    success: Boolean = false
): Room {
    val room = Room(
        title = title,
        description = description,
        avatarKey = imageKey,
        avatarVersion = imageVersion,
        type = type,
        inviteCode = inviteCode,
        memberCount = memberCount
    )
    ReflectionTestUtils.setField(room, "id", id)
    ReflectionTestUtils.setField(room, "success", success)

    return room
}

fun createUserRoom(
    user: User,
    room: Room,
    roomRole: RoomRole = USER_ROOM_ROLE,
    lastReadMessageId: Long? = USER_ROOM_LAST_READ_MESSAGE_ID,
    paymentStatus: PaymentStatus = USER_ROOM_PAYMENT_STATUS,
    id: Long = USER_ROOM_ID
): UserRoom {
    val userRoom = UserRoom(
        user = user,
        room = room,
        roomRole = roomRole,
        lastReadMessageId = lastReadMessageId,
        paymentStatus = paymentStatus
    )
    ReflectionTestUtils.setField(userRoom, "id", id)

    return userRoom
}

fun createAssignment(
    room: Room,
    title: String = ASSIGNMENT_TITLE,
    description: String = ASSIGNMENT_DESCRIPTION,
    due: Instant = ASSIGNMENT_DUE,
    status: AssignmentStatus = ASSIGNMENT_STATUS,
    punished: Boolean = ASSIGNMENT_PUNISHED,
    id: Long = ASSIGNMENT_ID
): Assignment {
    val assignment = Assignment(
        room = room,
        title = title,
        description = description,
        due = due,
        status = status,
        punished = punished
    )
    ReflectionTestUtils.setField(assignment, "id", id)

    return assignment
}
