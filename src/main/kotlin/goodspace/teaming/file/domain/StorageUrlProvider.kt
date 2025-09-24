package goodspace.teaming.file.domain

interface StorageUrlProvider {
    /** 이미지/미리보기 등 브라우저가 바로 볼 리소스용 URL */
    fun publicUrl(key: String?, version: Int? = null, size: Int? = null): String?

    /** 다운로드/저장용 URL (파일명 파라미터 등 포함) */
    fun downloadUrl(key: String?, filename: String? = null, version: Int? = null): String?
}
