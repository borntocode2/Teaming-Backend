package goodspace.teaming.global.entity.room

enum class RoomType(
    val typeName: String,
    val price: Int,
    val description: String
) {
    BASIC(
        "Basic Room",
        2060,
        "메가커피 아이스 아메리카노 1개"
    ),
    STANDARD(
        "Standard Room",
        4840,
        "스타벅스 아이스 아메리카노 1개"
    ),
    ELITE(
        "Elite Room",
        8240,
        "스타벅스 아이스 아메리카노 1개, 프렌치 크루아상 1개"
    );
}
