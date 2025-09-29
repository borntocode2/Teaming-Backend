package goodspace.teaming.file.domain

import java.time.format.DateTimeFormatter

object FileConstants {
    const val BYTES_PER_MB: Long = 1024L * 1024L
    const val MAX_UPLOAD_SIZE_MB: Long = 25L
    const val MIN_OBJECT_BYTES: Long = 1L
    const val SAFE_NAME_MAX_LENGTH: Int = 120
    val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM")
    val ALLOWED_MIME_PREFIXES = listOf(
        // 미디어
        "image/",
        "video/",
        "audio/",

        // 문서
        "application/pdf",
        "application/haansoftpdf",
        "application/haansofthwp",
        "application/haansoftpptx",
        "application/x-hwp",
        "application/x-hwpx",

        // MS Office
        "application/msword",
        "application/vnd.ms-excel",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/vnd.ms-excel.sheet.macroEnabled.12",
        "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
        "application/vnd.ms-word.document.macroEnabled.12",

        // 오픈 문서
        "application/vnd.oasis.opendocument.text",
        "application/vnd.oasis.opendocument.spreadsheet",
        "application/vnd.oasis.opendocument.presentation",

        // 기타 문서
        "application/rtf",
        "application/xml",
        "text/plain",
        "text/csv",
        "application/json",

        // 압축
        "application/zip",
        "application/x-zip-compressed",
        "application/x-7z-compressed",
        "application/x-rar-compressed",
        "application/x-tar",
        "application/x-alz-compressed",
        "application/x-egg",
        "application/x-egg-compressed"
    )
}
