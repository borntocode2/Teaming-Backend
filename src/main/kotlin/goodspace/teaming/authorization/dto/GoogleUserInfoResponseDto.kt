package goodspace.teaming.authorization.dto

import goodspace.teaming.global.entity.user.TeamingUser

class GoogleUserInfoResponseDto (
    val id: String,
    val email: String,
    val name: String,
    val picture: String,
    ){
    fun toEntity(): TeamingUser {
        return TeamingUser(
            password = "임시패스워드",
            identifier = id,

        )
    }
}

