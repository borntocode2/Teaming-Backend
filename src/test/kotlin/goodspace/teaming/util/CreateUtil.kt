package goodspace.teaming.util

import goodspace.teaming.fixture.USER_EMAIL
import goodspace.teaming.fixture.USER_ID
import goodspace.teaming.fixture.USER_NAME
import goodspace.teaming.fixture.USER_PASSWORD
import goodspace.teaming.global.entity.user.TeamingUser
import org.springframework.test.util.ReflectionTestUtils

fun createUser(
    email: String = USER_EMAIL,
    name: String = USER_NAME,
    password: String = USER_PASSWORD,
    id: Long = USER_ID
): TeamingUser {
    val user = TeamingUser(
        email = email,
        name = name,
        password = password
    )
    ReflectionTestUtils.setField(user, "id", id)

    return user
}
