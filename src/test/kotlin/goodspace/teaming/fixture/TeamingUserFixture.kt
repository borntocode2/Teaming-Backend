package goodspace.teaming.fixture

import goodspace.teaming.global.entity.user.TeamingUser


enum class TeamingUserFixture(
    private val email: String,
    private val password: String,
    private val username: String,
) {
    A(
        "a@email.com",
        "aPassword",
        "aUsername"
    );

    fun getInstance(): TeamingUser {
        return TeamingUser(
            email = email,
            password = password,
            name = username
        )
    }
}
