package goodspace.teaming.global.security

import goodspace.teaming.global.entity.user.Role
import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.security.Key
import java.util.*

@Component
class TokenProvider(
    @Value("\${keys.jwt.secret}") jwtSecret: String,
    @Value("\${keys.jwt.access-token-validity-in-milliseconds}") private val validityTime: Long
) {
    companion object {
        private const val TOKEN_TYPE_CLAIM = "PlanFit/TokenType"
        private const val BEARER = "Bearer "
        private const val AUTHORIZATION = "Authorization"
        private const val ROLES = "roles"
    }

    private val key: Key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret))

    fun createToken(id: Long, tokenType: TokenType, roles: List<Role>): String {
        val now = Date()
        val expiresAt = when (tokenType) {
            TokenType.ACCESS  -> Date(now.time + validityTime)
            TokenType.REFRESH -> Date(now.time + validityTime * 24)
        }

        return Jwts.builder()
            .setSubject(id.toString())
            .claim(TOKEN_TYPE_CLAIM, tokenType.name)
            .claim(ROLES, convertRolesToString(roles))
            .setIssuedAt(now)
            .setExpiration(expiresAt)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        val rolesString: String = claims.get(ROLES, String::class.java) ?: ""
        val authorities = rolesString
            .split(",")
            .filter { it.isNotBlank() }
            .map { SimpleGrantedAuthority(it) }

        return UsernamePasswordAuthenticationToken(claims.subject, null, authorities).also {
            it.details = claims
        }
    }

    /**
     * "Authorization: Bearer <token>" 헤더에서 토큰을 추출
     * 조건 불충족 시 null 반환
     */
    fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(AUTHORIZATION)
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER)) {
            bearerToken.substring(BEARER.length)
        } else {
            null
        }
    }

    /**
     * 서명/만료 검증 후, 요청한 TokenType과 일치하는지 추가 확인
     */
    fun validateToken(token: String, tokenType: TokenType): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            hasProperType(token, tokenType)
        } catch (_: UnsupportedJwtException) {
            false
        } catch (_: ExpiredJwtException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        } catch (_: MalformedJwtException) {
            false
        }
    }

    /**
     * 문자열 토큰을 Claims 로 변환
     * 만료된 토큰은 ExpiredJwtException에서 claims를 꺼내 반환
     */
    fun parseClaims(token: String): Claims {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: ExpiredJwtException) {
            e.claims
        } catch (e: SignatureException) {
            throw IllegalArgumentException("토큰 복호화 실패: 부적절한 토큰입니다.")
        }
    }

    fun getIdFromToken(token: String): Long {
        val subject = parseClaims(token).subject
        if (subject.isNullOrBlank()) {
            throw IllegalArgumentException("토큰에 사용자 ID 정보가 없습니다.")
        }
        return subject.toLongOrNull()
            ?: throw IllegalArgumentException("토큰의 subject 값을 Long으로 변환할 수 없습니다.")
    }

    /**
     * 토큰 타입이 기대한 타입과 일치하는지 확인
     */
    private fun hasProperType(token: String, tokenType: TokenType): Boolean {
        val claims = parseClaims(token)
        val tokenTypeClaim = claims[TOKEN_TYPE_CLAIM] as? String ?: return false
        return tokenType.name == tokenTypeClaim
    }

    private fun convertRolesToString(roles: List<Role>): String =
        roles.joinToString(",") { it.toString() }
}
