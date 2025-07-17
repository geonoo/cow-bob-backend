package com.logistics.repository

import com.logistics.entity.Driver
import com.logistics.entity.DriverStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.CacheEvict
import java.time.LocalDate

interface DriverRepository : JpaRepository<Driver, Long>, DriverRepositoryCustom {
    // @Cacheable(value = ["drivers"], key = "#status.name")
    fun findByStatus(status: DriverStatus): List<Driver>
    
    @CacheEvict(value = ["drivers"], allEntries = true)
    override fun <S : Driver> save(entity: S): S
    
    @CacheEvict(value = ["drivers"], allEntries = true)
    override fun deleteById(id: Long)
}