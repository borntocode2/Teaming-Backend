package goodspace.teaming.chat.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.lang.RuntimeException

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
class InviteCodeAllocationFailedException(
    override val message: String = "유일한 초대 코드를 생성하는 데 실패했습니다."
) : RuntimeException(message)
