package goodspace.teaming.chat.service

import goodspace.teaming.chat.domain.mapper.ChatMessageResponseMapper
import goodspace.teaming.chat.dto.ChatMessagePageResponseDto
import goodspace.teaming.chat.dto.ChatMessageResponseDto
import goodspace.teaming.chat.dto.ChatSendRequestDto
import goodspace.teaming.chat.event.ChatMessageCreatedEvent
import goodspace.teaming.global.entity.file.AntiVirusScanStatus
import goodspace.teaming.global.entity.file.Attachment
import goodspace.teaming.global.entity.file.File
import goodspace.teaming.global.entity.file.FileType
import goodspace.teaming.global.entity.room.*
import goodspace.teaming.global.entity.user.User
import goodspace.teaming.global.repository.FileRepository
import goodspace.teaming.global.repository.MessageRepository
import goodspace.teaming.global.repository.UserRoomRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val ROOM_NOT_FOUND = "티밍룸을 조회할 수 없습니다."
private const val NOT_PAID = "결제되지 않아 티밍룸에 엑세스할 수 없습니다."
private const val NOT_EXIST_FILE = "존재하지 않는 파일이 포함되어 있습니다."

@Service
class MessageServiceImpl(
    private val userRoomRepository: UserRoomRepository,
    private val messageRepository: MessageRepository,
    private val fileRepository: FileRepository,
    private val chatMessageResponseMapper: ChatMessageResponseMapper,
    private val eventPublisher: ApplicationEventPublisher,
    @Value("\${chat.recent-message.lower-bound:1}")
    private val recentMessageLowerBound: Int,
    @Value("\${chat.recent-message.upper-bound:200}")
    private val recentMessageUpperBound: Int,
) : MessageService {
    @Transactional
    override fun saveMessage(userId: Long, roomId: Long, requestDto: ChatSendRequestDto): ChatMessageResponseDto {
        val userRoom = userRoomRepository.findByRoomIdAndUserId(roomId, userId)
            ?: throw IllegalArgumentException(ROOM_NOT_FOUND)

        assertPaymentStatus(userRoom)

        val user = userRoom.user
        val room = userRoom.room

        val message = getExistsOrCreate(user, room, requestDto)

        val responseDto = chatMessageResponseMapper.map(message)
        eventPublisher.publishEvent(ChatMessageCreatedEvent(roomId, userId, responseDto))

        return responseDto
    }

    @Transactional(readOnly = true)
    override fun findMessages(
        userId: Long,
        roomId: Long,
        amount: Int,
        beforeMessageId: Long?
    ): ChatMessagePageResponseDto {
        val userRoom = userRoomRepository.findByRoomIdAndUserId(roomId, userId)
            ?: throw IllegalArgumentException(ROOM_NOT_FOUND)

        assertPaymentStatus(userRoom)

        val size = amount.coerceIn(recentMessageLowerBound, recentMessageUpperBound)
        val pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id"))

        val messageSlice = if (beforeMessageId == null) {
            messageRepository.findByRoomOrderByIdDesc(userRoom.room, pageable)
        } else {
            messageRepository.findByRoomAndIdLessThanOrderByIdDesc(userRoom.room, beforeMessageId, pageable)
        }

        return ChatMessagePageResponseDto(
            items = messageSlice.content.map { chatMessageResponseMapper.map(it) },
            hasNext = messageSlice.hasNext(),
            nextCursor = messageSlice.nextCursor()
        )
    }

    private fun getExistsOrCreate(user: User, room: Room, requestDto: ChatSendRequestDto): Message {
        val existMessage =
            messageRepository.findByClientMessageIdAndRoomAndSender(requestDto.clientMessageId, room, user)
        if (existMessage != null) {
            return existMessage
        }

        // 동시성 대비 try-catch
        return try {
            saveNewMessage(user, room, requestDto)
        } catch (exception: DataIntegrityViolationException) {
            messageRepository.findByClientMessageIdAndRoomAndSender(requestDto.clientMessageId, room, user)!!
        }
    }

    private fun saveNewMessage(user: User, room: Room, requestDto: ChatSendRequestDto): Message {
        val fileIds = requestDto.attachmentFileIdsInOrder.distinct()
        val files = fileRepository.findAllById(fileIds)
        validateFiles(files, fileIds, room, user)

        val message = Message(
            sender = user,
            room = room,
            content = requestDto.content?.trim(),
            type = findMessageTypeByFiles(files) ?: requestDto.type,
            clientMessageId = requestDto.clientMessageId
        )
        connectFilesWithMessage(message, files, fileIds)

        return messageRepository.save(message)
    }

    private fun validateFiles(files: List<File>, fileIds: List<Long>, room: Room, user: User) {
        require(files.size == fileIds.size) { NOT_EXIST_FILE }

        files.forEach { file ->
            require(file.room.id == room.id && file.uploaderId == user.id) { "이 방/사용자의 파일이 아닙니다." }
            require(file.antiVirusScanStatus != AntiVirusScanStatus.INFECTED) { "악성 파일로 차단된 첨부입니다." }
        }
    }

    private fun findMessageTypeByFiles(files: List<File>): MessageType? {
        return when {
            files.isEmpty() -> null
            files.any { it.type == FileType.VIDEO } -> MessageType.VIDEO
            files.any { it.type == FileType.AUDIO } -> MessageType.AUDIO
            files.all { it.type == FileType.IMAGE } -> MessageType.IMAGE
            else -> MessageType.FILE
        }
    }

    private fun connectFilesWithMessage(message: Message, files: List<File>, fileIds: List<Long>) {
        if (fileIds.isNotEmpty()) {
            val fileMap = files.associateBy { it.id }

            fileIds.forEachIndexed { idx, id ->
                val file = fileMap[id]!!
                message.attachments.add(Attachment(message = message, file = file, sortOrder = idx))
            }
        }
    }

    private fun Slice<Message>.nextCursor(): Long? {
        return this.content.lastOrNull()?.id
    }

    private fun assertPaymentStatus(userRoom: UserRoom) {
        require(userRoom.paymentStatus != PaymentStatus.NOT_PAID) { NOT_PAID }
    }
}
