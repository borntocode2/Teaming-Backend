package goodspace.teaming.global.storage

import goodspace.teaming.file.domain.CdnStorageUrlProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private const val CDN_BASE = "https://cdn.base.com"
private const val VERSION_PARAMETER_NAME = "v"
private const val SIZE_PARAMETER_NAME = "s"
private const val FILENAME_PARAMETER_NAME = "filename"

private const val DEFAULT_KEY = "defaultKey"
private const val DEFAULT_VERSION = 5
private const val DEFAULT_SIZE = 100
private const val DEFAULT_FILE_NAME = "file.pdf"

class CdnStorageUrlProviderTest {
    private val cdnStorageUrlProvider = CdnStorageUrlProvider(
        cdnBase =  CDN_BASE,
        versionParameterName =  VERSION_PARAMETER_NAME,
        sizeParameterName =  SIZE_PARAMETER_NAME,
        filenameParameterName =  FILENAME_PARAMETER_NAME
    )

    @Nested
    @DisplayName("publicUrl")
    inner class PublicUrl {
        @Test
        fun `key 값이 null이라면 null을 반환한다`() {
            val url = cdnStorageUrlProvider.publicUrl(
                key = null,
                version = DEFAULT_VERSION,
                size = DEFAULT_SIZE
            )

            assertThat(url).isNull()
        }

        @Test
        fun `key 값이 null이 아니라면 null을 반환하지 않는다`() {
            val url = cdnStorageUrlProvider.publicUrl(
                key = DEFAULT_KEY,
                version = null,
                size = null
            )

            assertThat(url).isNotNull()
        }

        @Test
        fun `버전, 사이즈 정보는 쿼리 스트링에 담긴다`() {
            val url = cdnStorageUrlProvider.publicUrl(
                key = DEFAULT_KEY,
                version = DEFAULT_VERSION,
                size = DEFAULT_SIZE,
            )!!

            val queryString = getQueryString(url)
            val params = parseQueryString(queryString)

            assertThat(params[VERSION_PARAMETER_NAME]).isEqualTo(DEFAULT_VERSION.toString())
            assertThat(params[SIZE_PARAMETER_NAME]).isEqualTo(DEFAULT_SIZE.toString())
        }

        @Test
        fun `파라미터가 없으면 쿼리 스트링을 생성하지 않는다`() {
            val url = cdnStorageUrlProvider.downloadUrl(
                key = DEFAULT_KEY,
                filename = null,
                version = null
            )!!

            assertThat(url).isNotEmpty()
            assertThat(getQueryString(url)).isEmpty()
        }
    }

    @Nested
    @DisplayName("downloadUrl")
    inner class DownloadUrl {
        @Test
        fun `key 값이 null이라면 null을 반환한다`() {
            val url = cdnStorageUrlProvider.downloadUrl(
                key = null,
                filename = DEFAULT_FILE_NAME,
                version = DEFAULT_VERSION
            )

            assertThat(url).isNull()
        }

        @Test
        fun `key 값이 null이 아니라면 null을 반환하지 않는다`() {
            val url = cdnStorageUrlProvider.downloadUrl(
                key = DEFAULT_KEY,
                filename = null,
                version = null
            )

            assertThat(url).isNotNull()
        }

        @Test
        fun `파일명, 버전 정보는 쿼리 스트링에 담긴다`() {
            val url = cdnStorageUrlProvider.downloadUrl(
                key = DEFAULT_KEY,
                filename = DEFAULT_FILE_NAME,
                version = DEFAULT_VERSION
            )!!

            val queryString = getQueryString(url)
            val params = parseQueryString(queryString)

            assertThat(params[FILENAME_PARAMETER_NAME]).isEqualTo(DEFAULT_FILE_NAME)
            assertThat(params[VERSION_PARAMETER_NAME]).isEqualTo(DEFAULT_VERSION.toString())
        }

        @Test
        fun `파라미터가 전혀 없으면 쿼리스트링 없이 반환한다`() {
            val url = cdnStorageUrlProvider.downloadUrl(
                key = DEFAULT_KEY,
                filename = null,
                version = null
            )!!

            assertThat(url).isNotEmpty()
            assertThat(getQueryString(url)).isEmpty()
        }
    }

    private fun getQueryString(url: String): String {
        val queryDelimiter = "?"

        if (!url.contains(queryDelimiter)) {
            return ""
        }

        return url.substringAfter(queryDelimiter)
    }

    private fun parseQueryString(queryString: String): Map<String, String> {
        if (queryString.isBlank()) return emptyMap()

        val result = LinkedHashMap<String, String>()
        queryString.split('&')
            .filter { it.isNotBlank() }
            .forEach { pair ->
                val indexOfSeparator = pair.indexOf('=')
                val rawKey = if (indexOfSeparator >= 0) pair.substring(0, indexOfSeparator) else pair
                val rawVal = if (indexOfSeparator >= 0) pair.substring(indexOfSeparator + 1) else ""
                val key = URLDecoder.decode(rawKey, StandardCharsets.UTF_8)
                val value = URLDecoder.decode(rawVal, StandardCharsets.UTF_8)

                result[key] = value
            }

        return result
    }
}
