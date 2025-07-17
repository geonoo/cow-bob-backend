package com.logistics.dto

import com.logistics.entity.Delivery
import com.logistics.entity.Driver
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

// 배송 등록/수정 요청용 DTO
data class DeliveryRequestDto(
    val destination: String,
    val address: String,
    val price: BigDecimal,
    val feedTonnage: Double, // Double로 받아서 BigDecimal로 변환
    val deliveryDate: LocalDate,
    val notes: String? = null,
    val driverId: Long? = null
) {
    // BigDecimal로 변환하는 확장 함수
    fun toBigDecimal(): BigDecimal = BigDecimal.valueOf(feedTonnage)
}

// 배송 응답용 DTO
data class DeliveryResponseDto(
    val id: Long,
    val destination: String,
    val address: String,
    val price: BigDecimal,
    val feedTonnage: BigDecimal,
    val deliveryDate: LocalDate,
    val driver: SimpleDriverDto?,
    val status: String,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val notes: String?
) {
    companion object {
        fun fromEntity(entity: Delivery): DeliveryResponseDto = DeliveryResponseDto(
            id = entity.id,
            destination = entity.destination,
            address = entity.address,
            price = entity.price,
            feedTonnage = entity.feedTonnage,
            deliveryDate = entity.deliveryDate,
            driver = entity.driver?.let { SimpleDriverDto.fromEntity(it) },
            status = entity.status.name,
            createdAt = entity.createdAt,
            completedAt = entity.completedAt,
            notes = entity.notes
        )
    }
}

data class SimpleDriverDto(
    val id: Long,
    val name: String,
    val vehicleNumber: String
) {
    companion object {
        fun fromEntity(entity: Driver): SimpleDriverDto = SimpleDriverDto(
            id = entity.id,
            name = entity.name,
            vehicleNumber = entity.vehicleNumber
        )
    }
} 