package com.logistics.dto

import java.math.BigDecimal
import java.time.YearMonth

data class DriverRevenueDto(
    val driverId: Long,
    val driverName: String,
    val yearMonth: YearMonth,
    val totalDeliveries: Int,
    val totalRevenue: BigDecimal,
    val totalTonnage: BigDecimal,
    val averageRevenuePerDelivery: BigDecimal
)

data class DriverRevenueRequestDto(
    val driverId: Long? = null,
    val yearMonth: YearMonth
) 