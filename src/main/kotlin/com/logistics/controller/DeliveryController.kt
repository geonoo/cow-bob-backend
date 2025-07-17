package com.logistics.controller

import com.logistics.dto.DeliveryRequestDto
import com.logistics.dto.DeliveryResponseDto
import com.logistics.dto.DriverRevenueDto
import com.logistics.dto.DriverRevenueRequestDto
import com.logistics.entity.Delivery
import com.logistics.entity.Driver
import com.logistics.service.DeliveryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.YearMonth

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = ["http://localhost:3000"])
class DeliveryController(
    private val deliveryService: DeliveryService
) {
    
    @GetMapping
    fun getAllDeliveries(): List<DeliveryResponseDto> =
        deliveryService.getAllDeliveries().map { DeliveryResponseDto.fromEntity(it) }
    
    @GetMapping("/{id}")
    fun getDeliveryById(@PathVariable id: Long): DeliveryResponseDto {
        return DeliveryResponseDto.fromEntity(deliveryService.getDeliveryById(id))
    }
    
    @PostMapping
    fun createDelivery(@RequestBody dto: DeliveryRequestDto): ResponseEntity<DeliveryResponseDto> {
        val created = deliveryService.createDelivery(dto)
        return ResponseEntity.status(HttpStatus.CREATED).body(DeliveryResponseDto.fromEntity(created))
    }
    
    @PutMapping("/{id}")
    fun updateDelivery(@PathVariable id: Long, @RequestBody dto: DeliveryRequestDto): DeliveryResponseDto {
        return DeliveryResponseDto.fromEntity(deliveryService.updateDelivery(id, dto))
    }
    
    @DeleteMapping("/{id}")
    fun deleteDelivery(@PathVariable id: Long): ResponseEntity<Void> {
        deliveryService.deleteDelivery(id)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/pending")
    fun getPendingDeliveries(): List<Delivery> = deliveryService.getPendingDeliveries()
    
    @PostMapping("/{id}/recommend-driver")
    fun recommendDriverForDelivery(@PathVariable id: Long): ResponseEntity<Map<String, Any?>> {
        val recommendedDriver = deliveryService.recommendDriverForDelivery(id)
        return ResponseEntity.ok(mapOf(
            "recommendedDriver" to recommendedDriver?.let {
                mapOf(
                    "id" to it.id,
                    "name" to it.name,
                    "phoneNumber" to it.phoneNumber,
                    "vehicleNumber" to it.vehicleNumber,
                    "tonnage" to it.tonnage
                )
            }
        ))
    }
    
    @PostMapping("/{id}/assign/{driverId}")
    fun assignDeliveryToDriver(@PathVariable id: Long, @PathVariable driverId: Long): DeliveryResponseDto {
        return DeliveryResponseDto.fromEntity(deliveryService.assignDeliveryToDriver(id, driverId))
    }
    
    @PostMapping("/{id}/start")
    fun startDelivery(@PathVariable id: Long): DeliveryResponseDto {
        return DeliveryResponseDto.fromEntity(deliveryService.startDelivery(id))
    }
    
    @PostMapping("/{id}/complete")
    fun completeDelivery(@PathVariable id: Long): DeliveryResponseDto {
        return DeliveryResponseDto.fromEntity(deliveryService.completeDelivery(id))
    }
    
    /**
     * 기사별 월매출표 조회
     */
    @GetMapping("/revenue")
    fun getDriverRevenue(
        @RequestParam(required = false) driverId: Long?,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): List<DriverRevenueDto> {
        val request = DriverRevenueRequestDto(
            driverId = driverId,
            yearMonth = YearMonth.of(year, month)
        )
        return deliveryService.getDriverRevenue(request)
    }
    
    /**
     * 과거 배송 내역 조회 (완료된 배송만)
     */
    @GetMapping("/history")
    fun getDeliveryHistory(
        @RequestParam(required = false) driverId: Long?,
        @RequestParam(required = false) startDate: String?,
        @RequestParam(required = false) endDate: String?
    ): List<DeliveryResponseDto> {
        // TODO: 과거 배송 내역 조회 로직 구현
        // 현재는 모든 완료된 배송을 반환
        val allDeliveries = deliveryService.getAllDeliveries()
        val completedDeliveries = allDeliveries.filter { it.status.name == "COMPLETED" }
        
        return completedDeliveries.map { DeliveryResponseDto.fromEntity(it) }
    }
}