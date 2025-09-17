package goodspace.teaming.file.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URLEncoder
import java.time.Duration
import java.time.Instant

@Component
class S3PresignSupport(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucket: String,
    @Value("\${cloud.aws.uploads.presign-minutes:5}")
    private val presignMinutes: Long
) {
    private fun ttl(): Duration = Duration.ofMinutes(presignMinutes.coerceAtLeast(1))

    fun headWithChecksum(key: String): HeadObjectResponse =
        s3Client.headObject { it.bucket(bucket).key(key).checksumMode(ChecksumMode.ENABLED) }

    fun presignPut(key: String, contentType: String): String {
        val putReq = PutObjectRequest.builder()
            .bucket(bucket).key(key).contentType(contentType)
            .checksumAlgorithm(ChecksumAlgorithm.SHA256) // x-amz-checksum-sha256 강제
            .build()

        val pre = PutObjectPresignRequest.builder()
            .signatureDuration(ttl())
            .putObjectRequest(putReq)
            .build()

        return s3Presigner.presignPutObject(pre).url().toString()
    }

    fun presignGetWithDisposition(key: String, filename: String, contentType: String?): Pair<String, Long> {
        val encoded = URLEncoder.encode(filename, Charsets.UTF_8).replace("+", "%20")

        val getReqBuilder = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .responseContentDisposition("""attachment; filename="$filename"; filename*=UTF-8''$encoded""")

        if (!contentType.isNullOrBlank()) {
            getReqBuilder.responseContentType(contentType)
        }

        val getReq = getReqBuilder.build()

        val pre = GetObjectPresignRequest.builder()
            .signatureDuration(ttl())
            .getObjectRequest(getReq)
            .build()

        val url = s3Presigner.presignGetObject(pre).url().toString()
        val expiresAt = Instant.now().plus(ttl()).epochSecond
        return url to expiresAt
    }

    fun bucket(): String = bucket
}
