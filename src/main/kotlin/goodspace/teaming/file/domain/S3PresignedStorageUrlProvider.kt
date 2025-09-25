package goodspace.teaming.file.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.MetadataDirective
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration

@Component
class S3PresignedStorageUrlProvider(
    private val s3Presigner: S3Presigner,
    private val s3Client: S3Client,
    @Value("\${cloud.aws.s3.bucket}") private val bucket: String,
    @Value("\${cloud.aws.uploads.presign-minutes:5}") private val presignMinutes: Long
) : PresignedUploadUrlProvider {
    private fun ttl(): Duration = Duration.ofMinutes(presignMinutes.coerceAtLeast(1))

    /**
     * (체크섬 비활성화 버전)
     * - contentType만 서명에 포함
     * - checksumBase64 파라미터는 무시
     */
    override fun putUploadUrl(
        key: String,
        contentType: String,
    ): PresignedUploadUrlProvider.PresignedPut {
        val putReq = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)
            .overrideConfiguration { it.putHeader("Content-Type", contentType) }
            .build()

        val presignReq = PutObjectPresignRequest.builder()
            .signatureDuration(ttl())
            .putObjectRequest(putReq)
            .build()

        val url = s3Presigner.presignPutObject(presignReq).url().toString()
        val headers = mapOf("Content-Type" to contentType)

        return PresignedUploadUrlProvider.PresignedPut(url, headers)
    }

    /** (체크섬 비활성화) 일반 HEAD */
    fun head(key: String): HeadObjectResponse =
        s3Client.headObject { it.bucket(bucket).key(key) }

    fun bucket(): String = bucket

    /** Range GET (매직바이트 판별용) */
    fun getRangeBytes(key: String, startInclusive: Int, endInclusive: Int): ByteArray {
        val req = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .range("bytes=$startInclusive-$endInclusive")
            .build()

        s3Client.getObject(req).use { obj: ResponseInputStream<*> ->
            return obj.readAllBytes()
        }
    }

    /** in-place copy로 메타데이터 교체 */
    fun rewriteObjectMetadata(
        key: String,
        contentType: String,
        cacheControl: String? = null,
        contentDisposition: String? = null
    ) {
        val builder = CopyObjectRequest.builder()
            .sourceBucket(bucket)
            .sourceKey(key)
            .destinationBucket(bucket)
            .destinationKey(key)
            .metadataDirective(MetadataDirective.REPLACE)
            .contentType(contentType)

        if (!cacheControl.isNullOrBlank()) builder.cacheControl(cacheControl)
        if (!contentDisposition.isNullOrBlank()) builder.contentDisposition(contentDisposition)

        s3Client.copyObject(builder.build())
    }
}
