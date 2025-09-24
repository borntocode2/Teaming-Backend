package goodspace.teaming.authorization.service

import com.google.gson.Gson
import goodspace.teaming.authorization.dto.*
import goodspace.teaming.global.entity.user.OAuthUser
import goodspace.teaming.global.entity.user.Role
import goodspace.teaming.global.entity.user.UserType
import goodspace.teaming.global.repository.UserRepository
import goodspace.teaming.global.security.TokenProvider
import goodspace.teaming.global.security.TokenResponseDto
import goodspace.teaming.global.security.TokenType
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.Signature
import java.security.interfaces.ECPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.time.Instant
import java.util.*
import kotlin.math.min

private const val ACCESS_TOKEN_REQUEST_URL = "https://appleid.apple.com/auth/token"
private const val AUDIENCE = "https://appleid.apple.com"
private const val EXPO_TEST_AUD = "host.exp.Exponent"

private const val PEM_PREFIX_PKCS8 = "-----BEGIN PRIVATE KEY-----"
private const val PEM_SUFFIX_PKCS8 = "-----END PRIVATE KEY-----"
private const val PEM_PREFIX_EC = "-----BEGIN EC PRIVATE KEY-----"
private const val PEM_SUFFIX_EC = "-----END EC PRIVATE KEY-----"

private const val ILLEGAL_ID_TOKEN = "id token의 형식이 잘못되었습니다."
private const val INVALID_ISS = "id token의 ISS 속성 값이 부적절합니다."
private const val INVALID_AUD = "id token의 AUD 속성 값이 부적절합니다."
private const val EXPIRED_ID_TOKEN = "id token이 만료되었습니다."
private const val FUTURE_ID_TOKEN = "id token 발급 시점이 미래입니다."
private const val NOT_VERIFIED_EMAIL = "인증되지 않은 이메일입니다."
private const val DER_INVALID_SEQ = "DER 형식이 부적절합니다(SEQ 미존재)"
private const val DER_INVALID_R   = "DER 형식이 부적절합니다(r 누락)"
private const val DER_INVALID_S   = "DER 형식이 부적절합니다(s 누락)"
private const val DER_INVALID_LEN = "서명 변환 길이(outLen)가 올바르지 않습니다"

@Service
class AppleAuthService(
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider,
    private val urlEncoder: Base64.Encoder = Base64.getUrlEncoder(),
    private val urlDecoder: Base64.Decoder = Base64.getUrlDecoder(),
    private val standardDecoder: Base64.Decoder = Base64.getDecoder(),

    @Value("\${keys.apple.app-id}")
    private val appClientId: String,
    @Value("\${keys.apple.service-id}")
    private val webClientId: String,
    @Value("\${keys.apple.team-id}")
    private val teamId: String,
    @Value("\${keys.apple.key-id}")
    private val keyId: String,
    @Value("\${keys.apple.private-key-pem}")
    private val privateKeyPem: String
) {
    fun getAccessIdToken(requestDto: AppleOauthRequestDto): String {
        val clientSecret = createClientSecret(webClientId)

        val headers = getHeaders()
        val params = getParams(
            clientId = webClientId,
            clientSecret = clientSecret,
            code = requestDto.code,
            redirectUri = requestDto.redirectUri,
            codeVerifier = requestDto.codeVerifier
        )

        val response = sendAccessTokenRequest(headers, params)

        if (response.isFailed()) {
            throw RuntimeException("애플 토큰 교환에 실패했습니다. code = ${response.statusCode} params = $params")
        }

        val tokenResponse = response.getTokenResponse()
            ?: throw RuntimeException("애플 토큰 응답을 파싱하지 못했습니다. 응답 바디 = ${response.body}")

        return tokenResponse.idToken
    }

    @Transactional
    fun signInOrSignUp(requestDto: AppleSignInRequestDto): TokenResponseDto {
        val payload = parseIdTokenPayload(requestDto.accessIdToken)

        payload.validate()

        val identifier = payload.sub!!
        val user = userRepository.findByIdentifierAndUserType(identifier, UserType.APPLE)
            ?: saveNewUser(payload, requestDto.name)

        val accessToken = tokenProvider.createToken(user.id!!, TokenType.ACCESS, user.roles)
        val refreshToken = tokenProvider.createToken(user.id!!, TokenType.REFRESH, user.roles)
        user.token = refreshToken

        return TokenResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    private fun createClientSecret(clientId: String): String {
        try {
            val now = Instant.now().epochSecond
            val expiredAt = now.getExpiredAt()

            val header = createClientSecretHeader()
            val payload = createClientSecretPayload(now, expiredAt, clientId)

            val signingInput = "$header.$payload"
            val key = getECPrivateKeyFrom(privateKeyPem)

            val derSignature = signingInput.signEs256With(key)
            val joseSignature = derSignature.toJose(64)

            return "$signingInput.${joseSignature.base64UrlEncode()}"
        } catch (exception: Exception) {
            throw RuntimeException("Apple Client Secret 생성에 실패했습니다. ", exception)
        }
    }

    private fun getHeaders(): HttpHeaders {
        val headers = HttpHeaders()

        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        return headers
    }

    private fun getParams(
        clientId: String,
        clientSecret: String,
        code: String,
        redirectUri: String,
        codeVerifier: String?
    ): LinkedMultiValueMap<String, String> {
        val params = LinkedMultiValueMap<String, String>()

        params.add("grant_type", "authorization_code")
        params.add("client_id", clientId)
        params.add("client_secret", clientSecret)
        params.add("code", code)
        params.add("redirect_uri", redirectUri)
        if (!codeVerifier.isNullOrBlank()) {
            params.add("code_verifier", codeVerifier)
        }

        return params
    }

    private fun sendAccessTokenRequest(
        headers: HttpHeaders,
        params: LinkedMultiValueMap<String, String>
    ): ResponseEntity<String> {
        val requestEntity = HttpEntity(params, headers)

        return RestTemplate()
            .exchange(
                ACCESS_TOKEN_REQUEST_URL,
                HttpMethod.POST,
                requestEntity,
                String::class.java
            )
    }

    private fun createClientSecretHeader(): String {
        val headerJson = Gson().toJson(
            mapOf(
                "alg" to "ES256",
                "kid" to keyId,
                "typ" to "JWT"
            )
        )

        return headerJson.base64UrlEncode()
    }

    private fun createClientSecretPayload(now: Long, exp: Long, clientId: String): String {
        val payloadJson = Gson().toJson(
            mapOf(
                "iss" to teamId,
                "iat" to now,
                "exp" to exp,
                "aud" to AUDIENCE,
                "sub" to clientId
            )
        )

        return payloadJson.base64UrlEncode()
    }

    private fun parseIdTokenPayload(idToken: String): AppleIdTokenPayloadDto {
        val parts = idToken.split(".")
        require(parts.size >= 2) { ILLEGAL_ID_TOKEN }

        try {
            val payloadJson = String(urlDecoder.decode(parts[1]), StandardCharsets.UTF_8)

            return payloadJson.jsonToObject()
        } catch (exception: Exception) {
            throw RuntimeException("Apple ID Token을 파싱하지 못했습니다. $exception")
        }
    }

    private fun saveNewUser(payload: AppleIdTokenPayloadDto, name: String?): OAuthUser {
        val user = userRepository.save(OAuthUser(
            identifier = payload.sub!!,
            email = payload.email!!,
            name = name ?: "사용자",
            type = UserType.APPLE
        ))
        user.addRole(Role.USER)

        return user
    }

    /**
     * 최대 6개월의 exp를 생성한다
     */
    private fun Long.getExpiredAt(): Long {
        return this + 60L * 60L * 24L * 180L
    }

    private fun ByteArray.base64UrlEncode(): String {
        return urlEncoder.withoutPadding().encodeToString(this)
    }

    private fun String.base64UrlEncode(): String {
        val bytes = this.toByteArray(Charsets.UTF_8)

        return bytes.base64UrlEncode()
    }

    private fun getECPrivateKeyFrom(pem: String): ECPrivateKey {
        val trimmedPem = pem
            .trim()
            .removeSurrounding("\"")
            .replaceEscape()
            .replace(PEM_PREFIX_PKCS8, "")
            .replace(PEM_SUFFIX_PKCS8, "")
            .replace(PEM_PREFIX_EC, "")
            .replace(PEM_SUFFIX_EC, "")
            .filterNot { it.isWhitespace() }

        val pkcs8Der = standardDecoder.decode(trimmedPem)
        val keySpec = PKCS8EncodedKeySpec(pkcs8Der)

        return KeyFactory.getInstance("EC").generatePrivate(keySpec)
                as ECPrivateKey
    }

    private fun String.replaceEscape(): String {
        return this
            .replace("\\r", "\r")
            .replace("\\n", "\n")
    }

    private fun String.signEs256With(privateKey: ECPrivateKey): ByteArray {
        val bytes = this.toByteArray(StandardCharsets.UTF_8)
        val signature = Signature.getInstance("SHA256withECDSA")

        signature.initSign(privateKey)
        signature.update(bytes)

        return signature.sign()
    }

    private fun ByteArray.toJose(outLen: Int): ByteArray {
        require(outLen % 2 == 0 && outLen > 0) { "$DER_INVALID_LEN: $outLen" }

        var offset = 0
        require(this[offset++].toInt() == 0x30) { DER_INVALID_SEQ }
        val seqLen = this[offset++].toInt() and 0xFF
        if ((seqLen and 0x80) != 0) {
            val n = seqLen and 0x7F
            offset += n
        }

        require(this[offset++].toInt() == 0x02) { DER_INVALID_R }
        val rLen = this[offset++].toInt() and 0xFF
        val r = this.copyOfRange(offset, offset + rLen); offset += rLen

        require(this[offset++].toInt() == 0x02) { DER_INVALID_S }
        val sLen = this[offset++].toInt() and 0xFF
        val s = this.copyOfRange(offset, offset + sLen)

        val half = outLen / 2
        val out = ByteArray(outLen)
        copyLeftPadded(r.stripLeadingZeros(), out, 0, half)
        copyLeftPadded(s.stripLeadingZeros(), out, half, half)
        return out
    }

    private fun ByteArray.stripLeadingZeros(): ByteArray {
        val firstNonZeroIndex = this.indexOfFirst { it.toInt() != 0 }
            .takeIf { it >= 0 } ?: this.lastIndex // 모두 0이면 마지막 바이트만 유지

        return this.copyOfRange(firstNonZeroIndex, this.size)
    }

    private fun copyLeftPadded(src: ByteArray, dst: ByteArray, dstPos: Int, len: Int) {
        val copy = min(src.size.toDouble(), len.toDouble()).toInt()
        System.arraycopy(src, src.size - copy, dst, dstPos + len - copy, copy)
    }

    private fun <T> ResponseEntity<T>.isFailed(): Boolean {
        return !this.statusCode.is2xxSuccessful
    }

    private fun ResponseEntity<String>.getTokenResponse(): AppleTokenResponseDto? {
        return this.body?.jsonToObject()
    }

    private inline fun <reified T> String.jsonToObject(): T {
        return Gson().fromJson(this, T::class.java)
    }

    private fun AppleIdTokenPayloadDto.validate() {
        check(AUDIENCE == iss) { INVALID_ISS }
        check(webClientId == aud || appClientId == aud || EXPO_TEST_AUD == aud) { INVALID_AUD }

        val now = Instant.now().epochSecond
        check(exp != null && exp!! > now - 60) { EXPIRED_ID_TOKEN }
        check(iat != null && iat!! <= now + 60) { FUTURE_ID_TOKEN }

        check(emailVerified) {
            NOT_VERIFIED_EMAIL
        }
    }
}
