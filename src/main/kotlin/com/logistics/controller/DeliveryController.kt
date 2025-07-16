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
    fun getDeliveryById(@PathVariable id: Long): ResponseEntity<Delivery> {
        val delivery = deliveryService.getDeliveryById(id)
        return if (delivery != null) {
            ResponseEntity.ok(delivery)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping
    fun createDelivery(@RequestBody delivery: Delivery): ResponseEntity<Delivery> {
        val createdDelivery = deliveryService.createDelivery(delivery)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDelivery)
    }
    
    @PutMapping("/{id}")
    fun updateDelivery(@PathVariable id: Long, @RequestBody delivery: Delivery): ResponseEntity<Delivery> {
        val updatedDelivery = deliveryService.updateDelivery(id, delivery)
        return if (updatedDelivery != null) {
            ResponseEntity.ok(updatedDelivery)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteDelivery(@PathVariable id: Long): ResponseEntity<Void> {
        return if (deliveryService.deleteDelivery(id)) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/pending")
    fun getPendingDeliveries(): List<Delivery> = deliveryService.getPendingDeliveries()
    
    @PostMapping("/{id}/recommend-driver")
    fun recommendDriver(@PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        val delivery = deliveryService.getDeliveryById(id) ?: return ResponseEntity.notFound().build()
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
    fun assignDelivery(@PathVariable deliveryId: Long, @PathVariable driverId: Long): ResponseEntity<Delivery> {
        val assignedDelivery = deliveryService.assignDeliveryToDriver(deliveryId, driverId)
        return if (assignedDelivery != null) {
            ResponseEntity.ok(assignedDelivery)
        } else {
            ResponseEntity.badRequest().build()
        }
    }
    
    @PostMapping("/{id}/complete")
    fun completeDelivery(@PathVariable id: Long): ResponseEntity<Delivery> {
        val completedDelivery = deliveryService.completeDelivery(id)
        return if (completedDelivery != null) {
            ResponseEntity.ok(completedDelivery)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}