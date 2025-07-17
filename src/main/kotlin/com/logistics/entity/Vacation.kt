package com.logistics.entity

import com.fasterxml.jackson.annotation.JsonBackReference
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "vacations")
data class Vacation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    @JsonBackReference
    val driver: Driver,
    
    @Column(nullable = false)
    val startDate: LocalDate,
    
    @Column(nullable = false)
    val endDate: LocalDate,
    
    @Column(length = 500)
    val reason: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: VacationStatus = VacationStatus.PENDING,
    
    @Column(nullable = false)
    val requestDate: LocalDate = LocalDate.now()
)

enum class VacationStatus {
    PENDING, APPROVED, REJECTED
}