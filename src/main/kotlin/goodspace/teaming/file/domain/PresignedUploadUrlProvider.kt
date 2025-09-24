package goodspace.teaming.file.domain

interface PresignedUploadUrlProvider {
    data class PresignedPut(val url: String, val requiredHeaders: Map<String, String>)
    fun putUploadUrl(key: String, contentType: String, checksumBase64: String): PresignedPut
}
