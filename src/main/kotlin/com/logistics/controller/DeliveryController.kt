package com.logistics.controller

import com.logistics.entity.Delivery
import com.logistics.service.DeliveryService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = ["http://localhost:3000"])
class DeliveryController(
    private val deliveryService: DeliveryService
) {
    
    @GetMapping
    fun getAllDeliveries(): List<Delivery> = deliveryService.getAllDeliveries()
    
    @GetMapping("/{id}")
    fun getDeliveryById(@PathVariable id: Long): Delivery {
        return deliveryService.getDeliveryById(id)
    }
    
    @PostMapping
    fun createDelivery(@RequestBody delivery: Delivery): ResponseEntity<Delivery> {
        val createdDelivery = deliveryService.createDelivery(delivery)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDelivery)
    }
    
    @PutMapping("/{id}")
    fun updateDelivery(@PathVariable id: Long, @RequestBody delivery: Delivery): Delivery {
        return deliveryService.updateDelivery(id, delivery)
    }
    
    @DeleteMapping("/{id}")
    fun deleteDelivery(@PathVariable id: Long): ResponseEntity<Void> {
        deliveryService.deleteDelivery(id)
        return ResponseEntity.noContent().build()
    }
    
    @GetMapping("/pending")
    fun getPendingDeliveries(): List<Delivery> = deliveryService.getPendingDeliveries()
    
    @PostMapping("/{id}/recommend-driver")
    fun recommendDriver(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        val delivery = deliveryService.getDeliveryById(id)
        val recommendedDriver = deliveryService.recommendDriverForDelivery(delivery)
        
        return if (recommendedDriver != null) {
            ResponseEntity.ok(mapOf(
                "delivery" to delivery,
                "recommendedDriver" to recommendedDriver
            ))
        } else {
            ResponseEntity.ok(mapOf(
                "delivery" to delivery,
                "message" to "사용 가능한 기사가 없습니다."
            ))
        }
    }
    
    @PostMapping("/{deliveryId}/assign/{driverId}")
    fun assignDelivery(@PathVariable deliveryId: Long, @PathVariable driverId: Long): Delivery {
        return deliveryService.assignDeliveryToDriver(deliveryId, driverId)
    }
    
    @PostMapping("/{id}/complete")
    fun completeDelivery(@PathVariable id: Long): Delivery {
        return deliveryService.completeDelivery(id)
    }
}