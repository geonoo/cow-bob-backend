package com.logistics.controller

import com.logistics.entity.Vacation
import com.logistics.service.VacationService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/vacations")
@CrossOrigin(origins = ["http://localhost:3000"])
class VacationController(
    private val vacationService: VacationService
) {
    
    @GetMapping
    fun getAllVacations(): List<Vacation> = vacationService.getAllVacations()
    
    @GetMapping("/{id}")
    fun getVacationById(@PathVariable id: Long): ResponseEntity<Vacation> {
        val vacation = vacationService.getVacationById(id)
        return if (vacation != null) {
            ResponseEntity.ok(vacation)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping
    fun createVacation(@RequestBody vacation: Vacation): ResponseEntity<Vacation> {
        val createdVacation = vacationService.createVacation(vacation)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVacation)
    }
    
    @PutMapping("/{id}")
    fun updateVacation(@PathVariable id: Long, @RequestBody vacation: Vacation): ResponseEntity<Vacation> {
        val updatedVacation = vacationService.updateVacation(id, vacation)
        return if (updatedVacation != null) {
            ResponseEntity.ok(updatedVacation)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @DeleteMapping("/{id}")
    fun deleteVacation(@PathVariable id: Long): ResponseEntity<Void> {
        return if (vacationService.deleteVacation(id)) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @GetMapping("/driver/{driverId}")
    fun getVacationsByDriverId(@PathVariable driverId: Long): List<Vacation> = 
        vacationService.getVacationsByDriverId(driverId)
    
    @PostMapping("/{id}/approve")
    fun approveVacation(@PathVariable id: Long): ResponseEntity<Vacation> {
        val approvedVacation = vacationService.approveVacation(id)
        return if (approvedVacation != null) {
            ResponseEntity.ok(approvedVacation)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @PostMapping("/{id}/reject")
    fun rejectVacation(@PathVariable id: Long): ResponseEntity<Vacation> {
        val rejectedVacation = vacationService.rejectVacation(id)
        return if (rejectedVacation != null) {
            ResponseEntity.ok(rejectedVacation)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}