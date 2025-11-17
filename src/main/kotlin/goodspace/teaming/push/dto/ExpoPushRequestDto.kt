package goodspace.teaming.push.dto

data class ExpoPushRequestDto(
    val to: String,
    val title: String,
    val body: String,
    // 알림 클릭 시 앱에서 사용할 데이터
    val data: Map<String, Any>? = null
)
