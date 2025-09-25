package goodspace.teaming.gifticon.service

import goodspace.teaming.gifticon.Entity.*
import goodspace.teaming.gifticon.repository.GifticonRepository
import goodspace.teaming.global.entity.room.RoomType
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class GifticonService (
    private val gifticonRepository: GifticonRepository,
    private val userRepository: UserRepository
){
    @Transactional
    fun sendGifticon(user: User, roomType: RoomType){
        val grade = mapRoomTypeToGrade(roomType)
        val gifticon: Gifticon = gifticonRepository.findFirstByGradeAndUsedFalse(grade)
            ?: throw IllegalArgumentException("$grade 등급의 사용 가능한 기프티콘이 없습니다.")

        gifticon.isSent = true
        user.addGifticon(gifticon)

        gifticonRepository.save(gifticon)
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

    @Transactional(readOnly = true)
    fun getGifticonsByUserId(userId: Long): List<Gifticon> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("해당 회원을 찾을 수 없습니다.") }

        return user.gifticonList
    }

    fun checkExpiration(expiration: String) {
        if(expiration.isBlank() || expiration.length != 8 ||!(expiration.startsWith("202"))) {
            throw IllegalStateException("${expiration}은 기프티콘 만료기한 format이 아닙니다.")
        }
    }

    fun mapExpirationToLocalDateTime(expiration: String): LocalDateTime {
        checkExpiration(expiration)
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val date = LocalDate.parse(expiration, formatter)

        return date.atStartOfDay()
    }

    private fun mapRoomTypeToGrade(roomType: RoomType): Grade? {
        return when(roomType){
            RoomType.BASIC -> Grade.BASIC
            RoomType.STANDARD -> Grade.STANDARD
            RoomType.ELITE -> Grade.ELITE
            RoomType.DEMO -> null
        }
    }
}