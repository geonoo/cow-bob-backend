package com.logistics.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "deliveries")
data class Delivery(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    val destination: String,
    
    @Column(nullable = false)
    val address: String,
    
    @Column(nullable = false, precision = 10, scale = 2)
    val price: BigDecimal,
    
    @Column(nullable = false, precision = 5, scale = 2)
    val feedTonnage: BigDecimal = BigDecimal.ZERO,
    
    @Column(nullable = false)
    val deliveryDate: LocalDate,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    val driver: Driver? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: DeliveryStatus = DeliveryStatus.PENDING,
    
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    val assignedAt: LocalDateTime? = null,
    
    val startedAt: LocalDateTime? = null,
    
    val completedAt: LocalDateTime? = null,
    
    @Column(length = 1000)
    val notes: String? = null
)

enum class DeliveryStatus {
    PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
}