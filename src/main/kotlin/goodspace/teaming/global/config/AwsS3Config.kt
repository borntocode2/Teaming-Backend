package goodspace.teaming.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
class AwsS3Config(
    @Value("\${cloud.aws.region}")
    private val region: String,
    @Value("\${cloud.aws.s3.endpoint:}")
    private val endpoint: String?,
    @Value("\${cloud.aws.profile:s3-user}")
    private val profile: String
) {
    @Bean
    fun s3Client(): S3Client =
        S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(ProfileCredentialsProvider.create(profile))
            .let { applyEndpointIfPresent(it) }
            .build()

    @Bean
    fun s3Presigner(): S3Presigner =
        S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(ProfileCredentialsProvider.create(profile))
            .let { applyEndpointIfPresent(it) }
            .build()

    private fun applyEndpointIfPresent(builder: S3ClientBuilder): S3ClientBuilder =
        if (!endpoint.isNullOrBlank()) builder.endpointOverride(URI.create(endpoint)) else builder

    private fun applyEndpointIfPresent(builder: S3Presigner.Builder): S3Presigner.Builder =
        if (!endpoint.isNullOrBlank()) builder.endpointOverride(URI.create(endpoint)) else builder
}

