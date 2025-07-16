package com.logistics.service

import com.logistics.entity.Vacation
import com.logistics.entity.VacationStatus
import com.logistics.repository.VacationRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class VacationService(
    private val vacationRepository: VacationRepository
) {
    
    fun getAllVacations(): List<Vacation> = vacationRepository.findAll()
    
    fun getVacationById(id: Long): Vacation? = vacationRepository.findById(id).orElse(null)
    
    fun createVacation(vacation: Vacation): Vacation = vacationRepository.save(vacation)
    
    fun updateVacation(id: Long, updatedVacation: Vacation): Vacation? {
        return if (vacationRepository.existsById(id)) {
            vacationRepository.save(updatedVacation.copy(id = id))
        } else null
    }
    
    fun deleteVacation(id: Long): Boolean {
        return if (vacationRepository.existsById(id)) {
            vacationRepository.deleteById(id)
            true
        } else false
    }
    
    fun getVacationsByDriverId(driverId: Long): List<Vacation> = 
        vacationRepository.findByDriverIdAndStatus(driverId, VacationStatus.APPROVED)
    
    fun approveVacation(id: Long): Vacation? {
        val vacation = vacationRepository.findById(id).orElse(null) ?: return null
        val approvedVacation = vacation.copy(status = VacationStatus.APPROVED)
        return vacationRepository.save(approvedVacation)
    }
    
    fun rejectVacation(id: Long): Vacation? {
        val vacation = vacationRepository.findById(id).orElse(null) ?: return null
        val rejectedVacation = vacation.copy(status = VacationStatus.REJECTED)
        return vacationRepository.save(rejectedVacation)
    }
    
    fun isDriverOnVacation(driverId: Long, date: LocalDate): Boolean {
        return vacationRepository.findActiveVacationForDriverAndDate(driverId, date) != null
    }
}