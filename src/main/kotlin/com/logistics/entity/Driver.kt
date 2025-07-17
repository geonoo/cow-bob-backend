package com.logistics.entity

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "drivers")
data class Driver(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(nullable = false, unique = true)
    val phoneNumber: String,
    
    @Column(nullable = false)
    val vehicleNumber: String,
    
    @Column(nullable = false)
    val vehicleType: String,
    
    @Column(nullable = false)
    val tonnage: Double,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: DriverStatus = DriverStatus.ACTIVE,
    
    @Column(nullable = false)
    val joinDate: LocalDate = LocalDate.now(),
    
    @OneToMany(mappedBy = "driver", cascade = [CascadeType.ALL])
    @JsonManagedReference
    val vacations: List<Vacation> = emptyList(),
    
    @OneToMany(mappedBy = "driver", cascade = [CascadeType.ALL])
    @JsonManagedReference
    val deliveries: List<Delivery> = emptyList()
)

enum class DriverStatus {
    ACTIVE, INACTIVE, ON_VACATION
}