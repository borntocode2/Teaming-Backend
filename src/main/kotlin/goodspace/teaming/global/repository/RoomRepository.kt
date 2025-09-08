package goodspace.teaming.global.repository

import goodspace.teaming.global.entity.room.Room
import org.springframework.data.jpa.repository.JpaRepository

interface RoomRepository : JpaRepository<Room, Long>
