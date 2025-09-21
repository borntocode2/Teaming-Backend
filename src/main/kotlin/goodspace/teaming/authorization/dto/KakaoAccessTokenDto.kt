package goodspace.teaming.authorization.dto

import com.fasterxml.jackson.annotation.JsonProperty

class KakaoAccessTokenDto (
    @JsonProperty("access_token")
    val accessToken: String
)