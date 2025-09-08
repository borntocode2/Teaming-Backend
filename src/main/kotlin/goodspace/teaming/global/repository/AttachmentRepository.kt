package goodspace.teaming.global.repository

import goodspace.teaming.global.entity.file.Attachment
import org.springframework.data.jpa.repository.JpaRepository

interface AttachmentRepository : JpaRepository<Attachment, Long>
