package goodspace.teaming.file.domain

import java.time.format.DateTimeFormatter

object FileConstants {
    const val BYTES_PER_MB: Long = 1024L * 1024L
    const val MAX_UPLOAD_SIZE_MB: Long = 25L
    const val MIN_OBJECT_BYTES: Long = 1L
    const val SAFE_NAME_MAX_LENGTH: Int = 120
    val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM")
    val ALLOWED_MIME_PREFIXES = listOf(
        "image/",
        "video/",
        "audio/",

        // 문서
        "application/pdf",
        "application/pdf",
        "application/msword",
        "application/vnd.ms-excel",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // docx
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",       // xlsx
        "application/vnd.openxmlformats-officedocument.presentationml.presentation", // pptx
        "text/plain",
        "text/csv",
        "application/json",

        // 압축
        "application/zip",
        "application/x-zip-compressed",
        "application/x-7z-compressed",
        "application/x-rar-compressed",
        "application/x-tar",
    )
}
