package goodspace.teaming.authorization.dto

import com.google.gson.annotations.SerializedName

data class AppleIdTokenPayloadDto(
    var iss: String? = null,
    var sub: String? = null,
    var aud: String? = null,
    var iat: Long? = null,
    var exp: Long? = null,
    var email: String? = null,
    @SerializedName("email_verified")
    var emailVerified: Boolean? = null,
    @SerializedName("is_private_email")
    var isPrivateEmail: Boolean? = null
)
