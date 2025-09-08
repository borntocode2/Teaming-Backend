package goodspace.teaming.global.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CdnStorageUrlProvider(
    @Value("\${cdn.base}")
    private val cdnBase: String,
    @Value("\${cdn.parameter.version:v")
    private val versionParameterName: String,
    @Value("\${cdn.parameter.size:s")
    private val sizeParameterName: String,
    @Value("\${cdn.parameter.filename:filename")
    private val filenameParameterName: String,
) : StorageUrlProvider {
    override fun publicUrl(key: String?, version: Int?, size: Int?): String? {
        if (key.isNullOrBlank()) {
            return null
        }

        val params = buildList {
            version?.let { add("$versionParameterName=$it") }
            size?.let { add("$sizeParameterName=$it") }
        }.joinToString("&").let { if (it.isBlank()) "" else "?$it" }

        return "$cdnBase/$key$params"
    }

    override fun downloadUrl(key: String?, filename: String?, version: Int?): String? {
        if (key.isNullOrBlank()) {
            return null
        }

        val queryString = buildList {
            version?.let { add("$versionParameterName=$it") }
            filename?.let { add("$filenameParameterName=" + java.net.URLEncoder.encode(it, java.nio.charset.StandardCharsets.UTF_8)) }
        }.joinToString("&").let { if (it.isBlank()) "" else "?$it" }

        return "$cdnBase/$key$queryString"
    }
}
