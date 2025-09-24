package goodspace.teaming.authorization.dto

import com.google.gson.annotations.SerializedName

data class AppleTokenResponseDto(
    @SerializedName("access_token")
    var accessToken: String? = null,

    @SerializedName("id_token")
    var idToken: String,

    @SerializedName("refresh_token")
    var refreshToken: String? = null,

    @SerializedName("token_type")
    var tokenType: String? = null,

    @SerializedName("expires_in")
    var expiresIn: Long? = null
)
