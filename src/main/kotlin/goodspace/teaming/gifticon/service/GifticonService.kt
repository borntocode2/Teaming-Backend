package goodspace.teaming.gifticon.service

import goodspace.teaming.gifticon.Entity.*
import goodspace.teaming.gifticon.repository.GifticonRepository
import goodspace.teaming.global.entity.user.User
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class GifticonService (
    private val gifticonRepository: GifticonRepository
){
    fun sendGifticon(user: User){
        // TODO: 유저 방 등급 조회 ->  -> 방 등급에 따른 기프티콘 return. etc.유저에게 기프티콘리스트 필드가 있어야할 듯.
    }

    @Transactional
    fun saveGifticon(code: String, expiration: String, grade: Grade) {
        if (grade==Grade.BASIC){
            gifticonRepository.save(BasicGifticon(
                code = code,
                expirationDate = mapExpirationToLocalDateTime(expiration),
                grade = Grade.BASIC
            ))
        }else if(grade==Grade.STANDARD){
            gifticonRepository.save(StandardGifticon(
                code = code,
                expirationDate = mapExpirationToLocalDateTime(expiration),
                grade = Grade.STANDARD
            ))
        }
        else if(grade==Grade.ELITE){
            gifticonRepository.save(EliteGifticon(
                code = code,
                expirationDate = mapExpirationToLocalDateTime(expiration),
                grade = Grade.ELITE
            ))
        }
        else{throw IllegalArgumentException("${grade}는 적절한 기프티콘 등급이 아닙니다.")}
    }

    fun checkExpiration(expiration: String) {
        if(expiration.isBlank() || expiration.length != 8 ||!(expiration.startsWith("202"))) {
            throw IllegalStateException("${expiration}은 기프티콘 만료기한 format이 아닙니다.")
        }
    }

    fun mapExpirationToLocalDateTime(expiration: String): LocalDateTime {
        checkExpiration(expiration)
        return LocalDateTime.parse(expiration)
    }
}