package goodspace.teaming.authorization.dto

import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor

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