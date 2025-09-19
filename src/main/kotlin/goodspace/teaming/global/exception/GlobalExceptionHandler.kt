package goodspace.teaming.global.exception

import io.jsonwebtoken.JwtException
import jakarta.persistence.EntityNotFoundException
import org.hibernate.exception.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.sql.SQLException
import org.springframework.http.HttpStatus.*

@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    @ExceptionHandler(value = [NoHandlerFoundException::class, NoResourceFoundException::class])
    fun handleNotFound(exception: Exception): ResponseEntity<String> {
        log.info("[ERROR RESPONSE] handleNotFound", exception)
        return ResponseEntity.status(NOT_FOUND).body("존재하지 않는 엔드포인트입니다")
    }

    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientError(exception: HttpClientErrorException): ResponseEntity<String> {
        log.info("[ERROR RESPONSE] HTTP Client exception", exception)
        return ResponseEntity.status(BAD_REQUEST).body("잘못된 값으로 인해 외부 API와 통신에 실패했습니다.")
    }

    @ExceptionHandler(HttpServerErrorException::class)
    fun handleHttpServerError(exception: HttpServerErrorException): ResponseEntity<String> {
        log.info("[ERROR RESPONSE] HTTP Server exception", exception)
        return ResponseEntity.status(BAD_GATEWAY).body("외부 API 불량으로 통신에 실패했습니다.")
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleBeanValidation(exception: MethodArgumentNotValidException): ResponseEntity<String> {
        log.info("[ERROR RESPONSE] bean validation", exception)
        val fieldError = exception.bindingResult.fieldErrors.first()
        return ResponseEntity.status(BAD_REQUEST).body(fieldError.defaultMessage)
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFound(exception: EntityNotFoundException): ResponseEntity<String> {
        log.info("[ERROR RESPONSE] entity not found", exception)
        return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(exception.message)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(exception: IllegalArgumentException): ResponseEntity<String> {
        log.info("[ERROR RESPONSE] illegal argument", exception)
        return ResponseEntity.status(BAD_REQUEST).body("Illegal argument: ${exception.message}")
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalState(exception: IllegalStateException): ResponseEntity<String> {
        log.info("[ERROR RESPONSE] illegal state", exception)
        return ResponseEntity.status(BAD_REQUEST).body("Illegal state: ${exception.message}")
    }

    @ExceptionHandler(JwtException::class)
    fun handleIllegalJwt(exception: JwtException): ResponseEntity<String> {
        log.info("[ERROR RESPONSE] illegal jwt", exception)
        return ResponseEntity.status(UNAUTHORIZED).body("부적절한 JWT 토큰입니다: ${exception.message}")
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleIllegalJson(exception: HttpMessageNotReadableException): ResponseEntity<String> {
        log.info("[ERROR RESPONSE] http message not readable", exception)
        return ResponseEntity.status(BAD_REQUEST).body("JSON 파싱에 실패했습니다: ${exception.message}")
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleIllegalHttpMethod(exception: HttpRequestMethodNotSupportedException): ResponseEntity<String> {
        log.info("[ERROR RESPONSE] http message not supported", exception)
        return ResponseEntity.status(METHOD_NOT_ALLOWED).body("부적절한 HTTP 메서드입니다: ${exception.message}")
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleIllegalMediaType(exception: HttpMediaTypeNotSupportedException): ResponseEntity<String> {
        log.info("[ERROR RESPONSE] media type not supported", exception)
        return ResponseEntity.status(METHOD_NOT_ALLOWED).body("부적절한 Content-Type 혹은 Accept입니다: ${exception.message}")
    }

    @ExceptionHandler(SQLException::class)
    fun handleSQLException(exception: SQLException): ResponseEntity<String> {
        log.warn("[ERROR RESPONSE] sql exception", exception)
        return ResponseEntity.status(CONFLICT).body("SQL exception: ${exception.message}")
    }

    @ExceptionHandler(value = [DataIntegrityViolationException::class, ConstraintViolationException::class])
    fun handleDataIntegrityViolation(exception: Exception): ResponseEntity<String> {
        log.warn("[ERROR RESPONSE] data integrity violation", exception)
        return ResponseEntity.status(CONFLICT).body("DB 무결성 제약조건에 위반됩니다: ${exception.message}")
    }

    @ExceptionHandler(Exception::class)
    fun handle(exception: Exception): ResponseEntity<String> {
        log.error("[ERROR RESPONSE] unexpected exception", exception)
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body("예상하지 못한 예외가 발생했습니다: ${exception.message}")
    }
}
