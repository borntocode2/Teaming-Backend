package goodspace.teaming.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
class AwsS3Config(
    @Value("\${cloud.aws.region}")
    private val awsRegionProperty: String,
    @Value("\${cloud.aws.s3.endpoint:}")
    private val awsS3EndpointProperty: String?
) {
    @Bean
    fun awsRegion(): Region = Region.of(awsRegionProperty)

    @Bean
    fun awsCredentialsProvider(): AwsCredentialsProvider =
        DefaultCredentialsProvider.builder().build()

    @Bean
    fun s3Client(awsRegion: Region, awsCredentialsProvider: AwsCredentialsProvider): S3Client =
        applyEndpointIfPresent(
            S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider)
        ).build()

    @Bean
    fun s3Presigner(awsRegion: Region, awsCredentialsProvider: AwsCredentialsProvider): S3Presigner =
        applyEndpointIfPresent(
            S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(awsCredentialsProvider)
        ).build()

    private fun applyEndpointIfPresent(builder: S3ClientBuilder): S3ClientBuilder =
        if (!awsS3EndpointProperty.isNullOrBlank()) builder.endpointOverride(URI.create(awsS3EndpointProperty)) else builder

    private fun applyEndpointIfPresent(builder: S3Presigner.Builder): S3Presigner.Builder =
        if (!awsS3EndpointProperty.isNullOrBlank()) builder.endpointOverride(URI.create(awsS3EndpointProperty)) else builder
}
