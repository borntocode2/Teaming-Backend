package goodspace.teaming.gifticon.service

import goodspace.teaming.gifticon.repository.GifticonRepository
import goodspace.teaming.global.entity.user.User
import org.springframework.stereotype.Service

@Service
class GifticonService (
    private val gifticonRepository: GifticonRepository
){
    fun sendGifticon(user: User){

    }
}