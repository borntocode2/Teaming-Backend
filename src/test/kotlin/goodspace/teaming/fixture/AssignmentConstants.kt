package goodspace.teaming.fixture

import goodspace.teaming.global.entity.aissgnment.AssignmentStatus
import java.time.Instant

const val ASSIGNMENT_ID = 1000L
const val ASSIGNMENT_TITLE = "과제 제목"
const val ASSIGNMENT_DESCRIPTION = "과제 설명"
val ASSIGNMENT_DUE = Instant.parse("2025-12-31T23:59:59Z")
val ASSIGNMENT_STATUS = AssignmentStatus.IN_PROGRESS
