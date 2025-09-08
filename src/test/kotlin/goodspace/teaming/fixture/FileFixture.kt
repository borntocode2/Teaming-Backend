package goodspace.teaming.fixture

import goodspace.teaming.global.entity.file.AntiVirusScanStatus
import goodspace.teaming.global.entity.file.File
import goodspace.teaming.global.entity.file.FileType
import goodspace.teaming.global.entity.file.TranscodeStatus
import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.user.User

enum class FileFixture(
    private val filename: String,
    private val type: FileType,
    private val mimeType: String,
    private val byteSize: Long,
    private val storageKey: String,
    private val storageBucket: String,
    private val checksumSha256: String,
    private val width: Int? = null,
    private val height: Int? = null,
    private val durationMs: Int? = null,
    private val thumbnailKey: String? = null,
    private val antiVirusScanStatus: AntiVirusScanStatus,
    private val transcodeStatus: TranscodeStatus
) {
    IMAGE(
        filename = "imagefile",
        type = FileType.IMAGE,
        mimeType = "image/jpeg",
        byteSize = 123,
        storageKey = "imageStorageKey",
        storageBucket = "imageStorageBuceket",
        checksumSha256 = "imageChecksum",
        width = 10,
        height = 10,
        thumbnailKey = "imageThumnail",
        antiVirusScanStatus = AntiVirusScanStatus.PASSED,
        transcodeStatus = TranscodeStatus.COMPLETED
    ),
    VIDEO(
        filename = "videofile",
        type = FileType.VIDEO,
        mimeType = "video/mp4",
        byteSize = 1_048_576, // 1MB
        storageKey = "videoStorageKey",
        storageBucket = "videoStorageBucket",
        checksumSha256 = "videoChecksum",
        width = 1920,
        height = 1080,
        durationMs = 120_000, // 2분
        thumbnailKey = "videoThumbnailKey",
        antiVirusScanStatus = AntiVirusScanStatus.PASSED,
        transcodeStatus = TranscodeStatus.COMPLETED
    );

    fun getInstanceWith(room: Room, user: User): File {
        return File(
            room = room,
            user = user,
            name = filename,
            type = type,
            mimeType = mimeType,
            byteSize = byteSize,
            storageKey = storageKey,
            storageBucket = storageBucket,
            checksumSha256 = checksumSha256,
            width = width,
            height = height,
            durationMs = durationMs,
            thumbnailKey = thumbnailKey,
            antiVirusScanStatus = antiVirusScanStatus,
            transcodeStatus = transcodeStatus
        )
    }
}
