package goodspace.teaming.file.event

import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class FileUploadedEventListener{
    @Async
    @EventListener
    fun handle(event: FileUploadedEvent) {
        // TODO: 업로드가 완료된 파일의 메타데이터를 채우고, 필요할 경우 썸네일을 만든다
        // TODO: 이미지의 경우 width/height를 추출하고, 썸네일을 생성해 업로드한다
        // TODO: 비디오의 경우 width/height/durationMs를 추출하고, 썸네일을 생성해 업로드한다
        // TODO: 오디오의 경우 durationMs를 추출한다
    }
}
