package goodspace.teaming.file.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant

@Component
class S3PresignSupport(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    @Value("\${cloud.aws.s3.bucket}") private val bucketName: String,
    @Value("\${cloud.aws.uploads.presign-minutes:5}") private val presignMinutes: Long
) {
    private fun ttl(): Duration = Duration.ofMinutes(presignMinutes.coerceAtLeast(1))

    /** (체크섬 비활성화) 일반 HEAD */
    fun head(key: String): HeadObjectResponse =
        s3Client.headObject { req -> req.bucket(bucketName).key(key) }

    /** 업로드용 Presigned PUT (체크섬 없이) */
    fun presignPut(key: String, contentType: String): String {
        val putReq = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(contentType)
            .build()

        val pre = PutObjectPresignRequest.builder()
            .signatureDuration(ttl())
            .putObjectRequest(putReq)
            .build()

        return s3Presigner.presignPutObject(pre).url().toString()
    }

    /** 다운로드용 Presigned GET (첨부) */
    fun presignGetWithDisposition(
        key: String,
        filename: String,
        contentType: String?
    ): Pair<String, Long> {
        val encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20")

        val getReqBuilder = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .responseContentDisposition("""attachment; filename="$filename"; filename*=UTF-8''$encoded""")

        if (!contentType.isNullOrBlank()) {
            getReqBuilder.responseContentType(contentType)
        }

        val pre = GetObjectPresignRequest.builder()
            .signatureDuration(ttl())
            .getObjectRequest(getReqBuilder.build())
            .build()

        val url = s3Presigner.presignGetObject(pre).url().toString()
        val expiresAt = Instant.now().plus(ttl()).epochSecond
        return url to expiresAt
    }

    fun bucket(): String = bucketName
}
