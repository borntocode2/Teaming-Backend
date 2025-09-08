package goodspace.teaming.global.repository

import goodspace.teaming.global.entity.file.File
import org.springframework.data.jpa.repository.JpaRepository

interface FileRepository : JpaRepository<File, Long>
