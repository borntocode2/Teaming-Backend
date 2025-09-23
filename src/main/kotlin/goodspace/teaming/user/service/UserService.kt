package goodspace.teaming.user.service

import goodspace.teaming.user.dto.*

interface UserService {
    fun getUserInfo(
        userId: Long
    ): UserInfoResponseDto

    fun updateEmail(
        userId: Long,
        requestDto: UpdateEmailRequestDto
    )

    fun updatePassword(
        userId: Long,
        requestDto: UpdatePasswordRequestDto
    )

    fun updateName(
        userId: Long,
        requestDto: UpdateNameRequestDto
    )

    fun removeUser(
        userId: Long
    )
}
