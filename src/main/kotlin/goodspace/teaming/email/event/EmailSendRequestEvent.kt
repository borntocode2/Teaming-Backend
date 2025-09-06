package goodspace.teaming.email.event

data class EmailSendRequestEvent(
    val to: String,
    val subject: String,
    val body: String
) {
    init {
        require(to.isNotBlank()) { "수신자(to)는 비어있을 수 없습니다." }
        require(subject.isNotBlank()) { "제목(subject)는 비어있을 수 없습니다." }
        require(body.isNotBlank()) { "본문(body)는 비어있을 수 없습니다." }
    }
}
