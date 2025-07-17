package com.logistics.dto

import com.logistics.entity.Driver
import java.time.LocalDate

// 기사 등록/수정 요청용 DTO
data class DriverRequestDto(
    val name: String,
    val phoneNumber: String,
    val vehicleNumber: String,
    val vehicleType: String,
    val tonnage: Double
)

// 기사 응답용 DTO (순환 참조 방지)
data class DriverResponseDto(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val vehicleNumber: String,
    val vehicleType: String,
    val tonnage: Double,
    val status: String,
    val joinDate: LocalDate
) {
    companion object {
        fun fromEntity(entity: Driver): DriverResponseDto = DriverResponseDto(
            id = entity.id,
            name = entity.name,
            phoneNumber = entity.phoneNumber,
            vehicleNumber = entity.vehicleNumber,
            vehicleType = entity.vehicleType,
            tonnage = entity.tonnage,
            status = entity.status.name,
            joinDate = entity.joinDate
        )
    }
} 