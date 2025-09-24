package goodspace.teaming.gifticon.repository

import goodspace.teaming.gifticon.Entity.Gifticon
import goodspace.teaming.gifticon.Entity.Grade
import org.springframework.data.jpa.repository.JpaRepository


interface GifticonRepository : JpaRepository<Gifticon, Long> {
    fun findFirstByGradeAndUsedFalse(grade: Grade?): Gifticon?
}