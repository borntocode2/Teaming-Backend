package goodspace.teaming.authorization.dto

import com.fasterxml.jackson.annotation.JsonProperty

class KakaoUserInfoResponseDto (
    val id: Long,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount
)
{

    // inner 대신 보통 data class 로 선언
    data class KakaoAccount(
        val email: String?,
        val name: String?,
        val profile: Profile?
    )

    data class Profile(
        @JsonProperty("profile_image_url")
        val profileImageUrl: String,
        @JsonProperty("thumbnail_image_url")
        val thumbnailImageUrl: String?
    )
}