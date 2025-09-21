package goodspace.teaming.authorization.dto

import com.fasterxml.jackson.annotation.JsonProperty

class NaverAccessTokenDto (
    @JsonProperty("access_token")
    val accessToken: String
)