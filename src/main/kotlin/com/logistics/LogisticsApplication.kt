package com.logistics

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class LogisticsApplication

fun main(args: Array<String>) {
    runApplication<LogisticsApplication>(*args)
}