package goodspace.teaming.authorization.dto

class NaverUserInfoResponseDto (
    val resultCode: String? = null,
    val message: String? = null,

    val response: Response
) {
    data class Response (
        val id: String,
        val email: String,
        val nickname: String
    )
}