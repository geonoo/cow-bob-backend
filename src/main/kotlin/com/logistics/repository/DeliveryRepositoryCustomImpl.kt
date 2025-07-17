package com.logistics.repository

import com.logistics.entity.Delivery
import com.logistics.entity.DeliveryStatus
import com.logistics.entity.QDelivery
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.YearMonth

@Repository
class DeliveryRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) : DeliveryRepositoryCustom {

    private val delivery = QDelivery.delivery

    override fun findDeliveriesByStatus(status: DeliveryStatus): List<Delivery> {
        return queryFactory
            .selectFrom(delivery)
            .where(delivery.status.eq(status))
            .orderBy(delivery.deliveryDate.asc())
            .fetch()
    }

    override fun findDeliveriesByDateRange(startDate: LocalDate, endDate: LocalDate): List<Delivery> {
        return queryFactory
            .selectFrom(delivery)
            .where(delivery.deliveryDate.between(startDate, endDate))
            .orderBy(delivery.deliveryDate.asc())
            .fetch()
    }

    override fun findDeliveriesByDriverAndDateRange(driverId: Long, startDate: LocalDate, endDate: LocalDate): List<Delivery> {
        return queryFactory
            .selectFrom(delivery)
            .where(
                delivery.driver.id.eq(driverId)
                    .and(delivery.deliveryDate.between(startDate, endDate))
            )
            .orderBy(delivery.deliveryDate.asc())
            .fetch()
    }

    override fun findCompletedDeliveriesByDriverAndYearMonth(driverId: Long, yearMonth: YearMonth): List<Delivery> {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        
        return queryFactory
            .selectFrom(delivery)
            .where(
                delivery.driver.id.eq(driverId)
                    .and(delivery.status.eq(DeliveryStatus.COMPLETED))
                    .and(delivery.deliveryDate.between(startDate, endDate))
            )
            .orderBy(delivery.deliveryDate.asc())
            .fetch()
    }

    override fun findCompletedDeliveriesByYearMonth(yearMonth: YearMonth): List<Delivery> {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        
        return queryFactory
            .selectFrom(delivery)
            .where(
                delivery.status.eq(DeliveryStatus.COMPLETED)
                    .and(delivery.deliveryDate.between(startDate, endDate))
            )
            .orderBy(delivery.deliveryDate.asc())
            .fetch()
    }

    override fun findDeliveriesByDriverAndStatus(driverId: Long, status: DeliveryStatus): List<Delivery> {
        return queryFactory
            .selectFrom(delivery)
            .where(
                delivery.driver.id.eq(driverId),
                delivery.status.eq(status)
            )
            .orderBy(delivery.deliveryDate.desc())
            .fetch()
    }

    override fun findDeliveriesByDestination(destination: String): List<Delivery> {
        return queryFactory
            .selectFrom(delivery)
            .where(
                delivery.destination.containsIgnoreCase(destination)
            )
            .orderBy(delivery.deliveryDate.desc())
            .fetch()
    }

    override fun countDeliveriesByStatus(status: DeliveryStatus): Long {
        return queryFactory
            .select(delivery.count())
            .from(delivery)
            .where(delivery.status.eq(status))
            .fetchOne() ?: 0L
    }

    override fun findRecentDeliveriesForDriverAndDestination(
        driverId: Long, 
        destination: String, 
        sinceDate: LocalDate
    ): List<Delivery> {
        return queryFactory
            .selectFrom(delivery)
            .where(
                delivery.driver.id.eq(driverId),
                delivery.destination.eq(destination),
                delivery.deliveryDate.goe(sinceDate)
            )
            .orderBy(delivery.deliveryDate.desc())
            .fetch()
    }
} 