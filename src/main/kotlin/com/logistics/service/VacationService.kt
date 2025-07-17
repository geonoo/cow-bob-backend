package com.logistics.service

import com.logistics.entity.Vacation
import com.logistics.entity.VacationStatus
import com.logistics.repository.VacationRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import com.logistics.dto.VacationRequestDto
import com.logistics.repository.DriverRepository
import com.logistics.exception.ResourceNotFoundException

@Service
class VacationService(
    private val vacationRepository: VacationRepository,
    private val driverRepository: DriverRepository
) {
    
    fun getAllVacations(): List<Vacation> = vacationRepository.findAll()
    
    fun getVacationById(id: Long): Vacation? = vacationRepository.findById(id).orElse(null)
    
    fun createVacation(dto: VacationRequestDto): Vacation {
        val driver = driverRepository.findById(dto.driverId).orElseThrow {
            ResourceNotFoundException("ID가 ${dto.driverId} 인 기사를 찾을 수 없습니다.")
        }
        
        val vacation = Vacation(
            driver = driver,
            startDate = dto.startDate,
            endDate = dto.endDate,
            reason = dto.reason
        )
        
        return vacationRepository.save(vacation)
    }
    
    fun updateVacation(id: Long, dto: VacationRequestDto): Vacation? {
        if (!vacationRepository.existsById(id)) {
            return null
        }
        
        val driver = driverRepository.findById(dto.driverId).orElseThrow {
            ResourceNotFoundException("ID가 ${dto.driverId} 인 기사를 찾을 수 없습니다.")
        }
        
        val vacation = Vacation(
            id = id,
            driver = driver,
            startDate = dto.startDate,
            endDate = dto.endDate,
            reason = dto.reason
        )
        
        return vacationRepository.save(vacation)
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