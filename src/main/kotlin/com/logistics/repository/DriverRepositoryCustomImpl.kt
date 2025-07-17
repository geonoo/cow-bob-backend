package com.logistics.repository

import com.logistics.entity.Driver
import com.logistics.entity.DeliveryStatus
import com.logistics.entity.QDriver
import com.logistics.entity.QVacation
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class DriverRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : DriverRepositoryCustom {

    override fun findAvailableDriversForDate(date: LocalDate): List<Driver> {
        val driver = QDriver.driver
        val vacation = QVacation.vacation
        
        return queryFactory
            .selectFrom(driver)
            .where(
                driver.status.eq(com.logistics.entity.DriverStatus.ACTIVE),
                driver.id.notIn(
                    queryFactory
                        .select(vacation.driver.id)
                        .from(vacation)
                        .where(
                            vacation.status.eq(com.logistics.entity.VacationStatus.APPROVED),
                            vacation.startDate.loe(date),
                            vacation.endDate.goe(date)
                        )
                )
            )
            .fetch()
    }

    override fun findDriversOrderByDeliveryCount(): List<Driver> {
        val driver = QDriver.driver
        
        return queryFactory
            .selectFrom(driver)
            .leftJoin(driver.deliveries)
            .where(driver.status.eq(com.logistics.entity.DriverStatus.ACTIVE))
            .groupBy(driver.id)
            .orderBy(driver.deliveries.size().asc())
            .fetch()
    }

    override fun findDriversByVehicleType(vehicleType: String): List<Driver> {
        val driver = QDriver.driver
        
        return queryFactory
            .selectFrom(driver)
            .where(driver.vehicleType.eq(vehicleType))
            .orderBy(driver.name.asc())
            .fetch()
    }

    override fun findDriversByTonnageRange(minTonnage: Double, maxTonnage: Double): List<Driver> {
        val driver = QDriver.driver
        
        return queryFactory
            .selectFrom(driver)
            .where(
                driver.tonnage.between(minTonnage, maxTonnage)
            )
            .orderBy(driver.tonnage.asc())
            .fetch()
    }
} 