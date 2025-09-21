package goodspace.teaming.authorization.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName

class GoogleAccessTokenDto (
    @SerializedName("access_token")
    val accessToken: String
)