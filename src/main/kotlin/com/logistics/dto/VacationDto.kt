package com.logistics.dto

import com.logistics.entity.Vacation
import java.time.LocalDate

// 휴가 신청 요청용 DTO
data class VacationRequestDto(
    val driverId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val reason: String? = null
)

// 휴가 응답용 DTO (순환 참조 방지)
data class VacationResponseDto(
    val id: Long,
    val driver: SimpleDriverDto,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val reason: String?,
    val status: String,
    val requestDate: LocalDate
) {
    companion object {
        fun fromEntity(entity: Vacation): VacationResponseDto = VacationResponseDto(
            id = entity.id,
            driver = SimpleDriverDto.fromEntity(entity.driver),
            startDate = entity.startDate,
            endDate = entity.endDate,
            reason = entity.reason,
            status = entity.status.name,
            requestDate = entity.requestDate
        )
    }
} 