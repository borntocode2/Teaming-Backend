package goodspace.teaming.gifticon.repository

import goodspace.teaming.gifticon.Entity.Gifticon
import org.springframework.data.jpa.repository.JpaRepository


interface GifticonRepository : JpaRepository<Gifticon, Int> {
}