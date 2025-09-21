package goodspace.teaming.authorization.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

class NaverAccessTokenDto (
    @SerializedName("access_token")
    val accessToken: String
)