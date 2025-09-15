package goodspace.teaming.fixture

import goodspace.teaming.global.entity.room.Room
import goodspace.teaming.global.entity.room.RoomType

enum class RoomFixture(
    private val title: String,
    private val type: RoomType,
    private val inviteCode: String
) {
    DEFAULT(
        "default title",
        RoomType.BASIC,
        "default invite"
    ),
    BASIC(
        "basic title",
        RoomType.BASIC,
        "basic invite"
    ),
    STANDARD(
        "stnadard title",
        RoomType.STANDARD,
        "standard invite"
    ),
    ELITE(
        "elite title",
        RoomType.ELITE,
        "elite invite"
    );

    fun getInstance(memberCount: Int = 5): Room {
        return Room(
            title = title,
            type = type,
            inviteCode = inviteCode,
            memberCount = memberCount
        )
    }
}
