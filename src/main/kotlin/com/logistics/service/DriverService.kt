package com.logistics.service

import com.logistics.entity.Driver
import com.logistics.entity.DriverStatus
import com.logistics.exception.*
import com.logistics.repository.DriverRepository
import com.logistics.dto.DriverRequestDto
import org.springframework.stereotype.Service
import java.time.LocalDate
import com.logistics.constant.ErrorMessage
import com.logistics.constant.MessageUtils
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable

@Service
class DriverService(
    private val driverRepository: DriverRepository
) {
    fun getAllDrivers(): List<Driver> = driverRepository.findAll()
    
    fun getDriverById(id: Long): Driver {
        return driverRepository.findById(id).orElseThrow {
            ResourceNotFoundException("ID가 $id 인 기사를 찾을 수 없습니다.")
        }
    }
    
    fun createDriver(dto: DriverRequestDto): Driver {
        try {
            val driver = Driver(
                name = dto.name,
                phoneNumber = dto.phoneNumber,
                vehicleNumber = dto.vehicleNumber,
                vehicleType = dto.vehicleType,
                tonnage = dto.tonnage
            )
            validateDriverData(driver)
            validateUniquePhoneNumber(driver.phoneNumber)
            return driverRepository.save(driver)
        } catch (e: Exception) {
            when (e) {
                is LogisticsException -> throw e
                else -> throw DataIntegrityException("기사 생성 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }
    
    fun updateDriver(id: Long, dto: DriverRequestDto): Driver {
        if (!driverRepository.existsById(id)) {
            throw ResourceNotFoundException("ID가 $id 인 기사를 찾을 수 없습니다.")
        }
        
        try {
            val driver = Driver(
                id = id,
                name = dto.name,
                phoneNumber = dto.phoneNumber,
                vehicleNumber = dto.vehicleNumber,
                vehicleType = dto.vehicleType,
                tonnage = dto.tonnage
            )
            validateDriverData(driver)
            validateUniquePhoneNumber(driver.phoneNumber, id)
            return driverRepository.save(driver)
        } catch (e: Exception) {
            when (e) {
                is LogisticsException -> throw e
                else -> throw DataIntegrityException("기사 수정 중 오류가 발생했습니다: ${e.message}")
            }
        }
    }
    
    fun deleteDriver(id: Long) {
        val driver = driverRepository.findById(id).orElseThrow {
            ResourceNotFoundException("ID가 $id 인 기사를 찾을 수 없습니다.")
        }
        
        // 배송 중인 기사는 삭제 불가
        if (driver.deliveries.any { it.status.name in listOf("ASSIGNED", "IN_PROGRESS") }) {
            throw BusinessRuleViolationException("배송 중인 기사는 삭제할 수 없습니다.")
        }
        
        try {
            driverRepository.deleteById(id)
        } catch (e: Exception) {
            throw DataIntegrityException("기사 삭제 중 오류가 발생했습니다: ${e.message}")
        }
    }
    
    fun getActiveDrivers(): List<Driver> = driverRepository.findByStatus(DriverStatus.ACTIVE)
    
    fun getAvailableDriversForDate(date: LocalDate): List<Driver> = 
        driverRepository.findAvailableDriversForDate(date)
    
    /**
     * 기사 데이터 검증
     */
    private fun validateDriverData(driver: Driver) {
        if (driver.name.isBlank()) {
            throw InvalidRequestException("기사 이름은 필수 입력 항목입니다.")
        }
        
        if (driver.phoneNumber.isBlank()) {
            throw InvalidRequestException("전화번호는 필수 입력 항목입니다.")
        }
        
        if (!isValidPhoneNumber(driver.phoneNumber)) {
            throw InvalidRequestException("올바른 전화번호 형식이 아닙니다.")
        }
        
        if (driver.vehicleNumber.isBlank()) {
            throw InvalidRequestException("차량번호는 필수 입력 항목입니다.")
        }
        
        if (driver.vehicleType.isBlank()) {
            throw InvalidRequestException("차량종류는 필수 입력 항목입니다.")
        }
        
        if (driver.tonnage <= 0) {
            throw InvalidRequestException("톤수는 0보다 커야 합니다.")
        }
        
        if (driver.joinDate.isAfter(LocalDate.now())) {
            throw InvalidRequestException("가입일은 오늘 이전이어야 합니다.")
        }
    }
    
    /**
     * 전화번호 중복 검증
     */
    private fun validateUniquePhoneNumber(phoneNumber: String, excludeId: Long? = null) {
        val existingDrivers = driverRepository.findAll()
            .filter { it.phoneNumber == phoneNumber }
            .filter { excludeId == null || it.id != excludeId }
        
        if (existingDrivers.isNotEmpty()) {
            throw BusinessRuleViolationException("이미 등록된 전화번호입니다.")
        }
    }
    
    /**
     * 전화번호 형식 검증
     */
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        val phoneRegex = Regex("^01[0-9]-[0-9]{4}-[0-9]{4}$")
        return phoneRegex.matches(phoneNumber)
    }
}