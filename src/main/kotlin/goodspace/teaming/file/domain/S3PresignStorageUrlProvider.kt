package goodspace.teaming.file.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration

@Component
class S3PresignStorageUrlProvider(
    private val s3Presigner: S3Presigner,
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String,
    @Value("\${cloud.aws.uploads.presign-minutes:5}") private val presignMinutes: Long
) : PresignedUploadUrlProvider {

    private fun ttl(): Duration = Duration.ofMinutes(presignMinutes.coerceAtLeast(1))

    override fun publicUrl(key: String?, version: Int?, size: Int?): String? {
        // presigned GET로 대응 (inline 미리보기용)
        if (key.isNullOrBlank()) return null
        val getReq = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()
        val pre = GetObjectPresignRequest.builder()
            .signatureDuration(ttl())
            .getObjectRequest(getReq)
            .build()
        return s3Presigner.presignGetObject(pre).url().toString()
    }

    override fun downloadUrl(key: String?, filename: String?, version: Int?): String? {
        if (key.isNullOrBlank()) return null
        val fname = filename ?: key.substringAfterLast('/')
        val encoded = URLEncoder.encode(fname, StandardCharsets.UTF_8).replace("+", "%20")

        val getReq = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .responseContentDisposition("""attachment; filename="$fname"; filename*=UTF-8''$encoded""")
            .build()

        val pre = GetObjectPresignRequest.builder()
            .signatureDuration(ttl())
            .getObjectRequest(getReq)
            .build()

        return s3Presigner.presignGetObject(pre).url().toString()
    }

    override fun putUploadUrl(key: String, contentType: String, checksumBase64: String): PresignedUploadUrlProvider.PresignedPut {
        val putReq = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .checksumAlgorithm(ChecksumAlgorithm.SHA256) // x-amz-checksum-sha256 강제
            .build()

        val pre = PutObjectPresignRequest.builder()
            .signatureDuration(ttl())
            .putObjectRequest(putReq)
            .build()

        val url = s3Presigner.presignPutObject(pre).url().toString()
        val headers = mapOf(
            "Content-Type" to contentType,
            "x-amz-checksum-sha256" to checksumBase64
        )
        return PresignedUploadUrlProvider.PresignedPut(url, headers)
    }

    // (선택) 헤더 검증용 HEAD 접근도 여기서 제공하고 싶다면:
    fun headWithChecksum(key: String): HeadObjectResponse =
        s3Client.headObject { it.bucket(bucket).key(key).checksumMode(ChecksumMode.ENABLED) }

    fun bucket(): String = bucket
}
