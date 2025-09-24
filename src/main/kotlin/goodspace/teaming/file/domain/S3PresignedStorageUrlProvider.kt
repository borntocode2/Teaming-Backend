package goodspace.teaming.file.domain

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.ChecksumAlgorithm
import software.amazon.awssdk.services.s3.model.ChecksumMode
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
     * 클라이언트가 보낸 Content-Type "원문 그대로"를 서명에 사용한다.
     * requiredHeaders에도 동일 문자열을 내려서 클라이언트가 그대로 보내도록 유도.
     */
    override fun putUploadUrl(
        key: String,
        contentType: String,
        checksumBase64: String
    ): PresignedUploadUrlProvider.PresignedPut {
        val putReq = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType)                        // ← 원문 그대로
            .checksumAlgorithm(ChecksumAlgorithm.SHA256)     // x-amz-checksum-sha256 사용
            .overrideConfiguration { cfg ->
                // 실제 업로드 시 반드시 포함돼야 하는 헤더들(서명에 포함됨)
                cfg.putHeader("Content-Type", contentType)
                cfg.putHeader("x-amz-checksum-sha256", checksumBase64)
            }
            .build()

        val presignReq = PutObjectPresignRequest.builder()
            .signatureDuration(ttl())
            .putObjectRequest(putReq)
            .build()

        val url = s3Presigner.presignPutObject(presignReq).url().toString()
        val headers = mapOf(
            "Content-Type" to contentType,
            "x-amz-checksum-sha256" to checksumBase64
        )
        return PresignedUploadUrlProvider.PresignedPut(url, headers)
    }

    /** 체크섬 포함 HEAD 조회 */
    fun headWithChecksum(key: String): HeadObjectResponse =
        s3Client.headObject { it.bucket(bucket).key(key).checksumMode(ChecksumMode.ENABLED) }

    fun bucket(): String = bucket

    /**
     * 객체 앞부분을 Range GET으로 가져온다 (매직바이트 스니핑용).
     * 예: getRangeBytes(key, 0, 1023)
     */
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

    /**
     * 같은 키로 copy-in-place 하면서 메타데이터를 교체한다.
     * - 서버가 판정한 안전한 Content-Type으로 강제
     * - 캐시 정책/디스포지션도 함께 세팅 가능
     */
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
            .destinationKey(key) // in-place copy
            .metadataDirective(MetadataDirective.REPLACE)
            .contentType(contentType)

        if (!cacheControl.isNullOrBlank()) {
            builder.cacheControl(cacheControl)
        }
        if (!contentDisposition.isNullOrBlank()) {
            builder.contentDisposition(contentDisposition)
        }

        s3Client.copyObject(builder.build())
    }
}
