package goodspace.teaming.authorization.dto

import com.fasterxml.jackson.annotation.JsonProperty

class GoogleAccessTokenDto (
    @JsonProperty("access_token")
    val accessToken: String
)