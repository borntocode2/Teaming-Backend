package goodspace.teaming.authorization.dto

import com.fasterxml.jackson.annotation.JsonProperty


class GoogleUserInfoResponseDto (
    val id: String,
    val email: String,
    val name: String? = null,

    @JsonProperty("picture")
    val pictureUrl: String? = null,
    val locale: String? = null
)

