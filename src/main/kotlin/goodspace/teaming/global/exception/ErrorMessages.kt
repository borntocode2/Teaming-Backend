package goodspace.teaming.global.exception

// User
const val USER_NOT_FOUND = "해당 회원을 조회할 수 없습니다."
const val ILLEGAL_TOKEN = "부적절한 토큰입니다."
const val EXPIRED_REFRESH_TOKEN = "만료된 리프레쉬 토큰입니다."
const val OAUTH_USER_CANNOT_CHANGE_PASSWORD = "소셜 회원은 이메일을 변경할 수 없습니다."
const val WRONG_PASSWORD = "비밀번호가 올바르지 않습니다."
const val ILLEGAL_PASSWORD = "부적절한 비밀번호입니다."

// Room
const val ROOM_NOT_FOUND = "티밍룸을 조회할 수 없습니다."
const val NOT_PAID = "결제되지 않아 티밍룸에 엑세스할 수 없습니다."
const val ALREADY_MEMBER_OF_ROOM = "이미 해당 티밍룸에 소속되어 있습니다."
const val NOT_MEMBER_OF_ROOM = "해당 티밍룸에 소속되어있지 않습니다."
const val NOT_LEADER = "팀장이 아닙니다."
const val ROOM_INACCESSIBLE = "해당 티밍룸에 접근할 수 없습니다."
const val WRONG_INVITE_CODE = "부적절한 초대 코드입니다."
const val CANNOT_LEAVE_BEFORE_SUCCESS = "팀플에 성공하기 전까진 나갈 수 없습니다."
const val ILLEGAL_ROOM_TITLE = "부적절한 티밍룸 제목입니다."
const val EVERY_MEMBER_NOT_ENTERED = "아직 모든 인원이 참여하지 않았습니다."
const val EVERY_MEMBER_NOT_PAID = "아직 결제가 완료되지 않은 팀원이 있습니다."
const val TEAMING_IN_PROGRESS = "아직 진행중인 팀 프로젝트가 있습니다."

// Assignment
const val ASSIGNMENT_NOT_FOUND = "과제를 찾을 수 없습니다."
const val MEMBER_TO_ASSIGN_NOT_FOUND = "과제를 할당할 회원을 조회할 수 없습니다."
const val NOT_ASSIGNED = "해당 과제에 할당되지 않았습니다."
const val CANCELED_ASSIGNMENT = "취소된 과제입니다."
const val COMPLETE_ASSIGNMENT = "이미 완료된 과제입니다."

// Email
const val EMAIL_NOT_FOUND = "이메일을 찾을 수 없습니다."
const val NOT_EXISTS_EMAIL = "존재하지 않는 이메일입니다."
const val ALREADY_EXISTS_EMAIL = "이미 사용 중인 이메일입니다."
const val EXPIRED_EMAIL_VERIFICATION = "이메일 인증이 만료되었습니다."
const val NOT_VERIFIED_EMAIL = "인증되지 않은 이메일입니다."
const val DUPLICATE_EMAIL = "이미 존재하는 이메일입니다."
const val WRONG_CODE = "코드가 올바르지 않습니다."

// File & S3
const val FILE_NOT_FOUND = "파일을 찾을 수 없습니다."
const val INCLUDE_NOT_EXIST_FILE = "존재하지 않는 파일이 포함되어 있습니다."
const val IMAGE_TOO_LARGE = "이미지 크기가 너무 큽니다."
const val UNSUPPORTED_IMAGE_TYPE = "지원하지 않는 이미지 형식입니다."
const val INVALID_UPLOAD_KEY = "업로드 키가 올바르지 않습니다."
const val S3_OBJECT_NOT_FOUND = "S3에 저장된 객체를 찾을 수 없습니다."
const val ORIGINAL_FILE_MISSING = "원본 파일이 존재하지 않습니다."
const val INVALID_SIZE = "파일 크기가 적절하지 않습니다."
const val INVALID_KEY_SCOPE = "Key에 방 정보와 회원 정보가 누락되었습니다."

// Gifticon
const val GIFTICON_NOT_FOUND = "기프티콘을 찾을 수 없습니다."
const val GIFTICON_ALREADY_SENT = "이미 사용자에게 전송된 기프티콘입니다."
