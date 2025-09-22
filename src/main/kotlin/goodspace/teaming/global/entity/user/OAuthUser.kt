package goodspace.teaming.global.entity.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import org.hibernate.annotations.SQLDelete

@Entity
@SQLDelete(
    sql = "UPDATE teaming_user " +
            "SET password = CONCAT('DELETED_', password) " +
            "WHERE id = ?"
)
class OAuthUser(
    @Column(nullable = false)
    val identifier: String,

    email: String,
    name: String,
    profileImageUrl: String?,
    thumbnailImageUrl: String?,

    // TODO : S3업로드에 저장할 정보를 회원 저장할 때 활용
    avatarKey: String? = null,
    avatarVersion: Int = 0,

    type: UserType
) : User(
    email = email,
    name = name,
    avatarKey = avatarKey,
    avatarVersion = avatarVersion,
    type = type
)
