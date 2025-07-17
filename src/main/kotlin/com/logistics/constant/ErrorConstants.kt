package com.logistics.constant

import org.springframework.http.HttpStatus

enum class ErrorType(val value: String) {
    RESOURCE_NOT_FOUND("Resource Not Found"),
    INVALID_REQUEST("Invalid Request"),
    BUSINESS_RULE_VIOLATION("Business Rule Violation"),
    DATA_INTEGRITY_ERROR("Data Integrity Error"),
    ASSIGNMENT_ERROR("Assignment Error"),
    INTERNAL_SERVER_ERROR("Internal Server Error")
}

enum class ErrorStatus(val status: HttpStatus) {
    NOT_FOUND(HttpStatus.NOT_FOUND),
    BAD_REQUEST(HttpStatus.BAD_REQUEST),
    CONFLICT(HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR)
}

data class ErrorInfo(
    val type: ErrorType,
    val status: ErrorStatus,
    val defaultMessage: String
) {
    companion object {
        val RESOURCE_NOT_FOUND = ErrorInfo(
            ErrorType.RESOURCE_NOT_FOUND,
            ErrorStatus.NOT_FOUND,
            "리소스를 찾을 수 없습니다."
        )

        val INVALID_REQUEST = ErrorInfo(
            ErrorType.INVALID_REQUEST,
            ErrorStatus.BAD_REQUEST,
            "잘못된 요청입니다."
        )

        val BUSINESS_RULE_VIOLATION = ErrorInfo(
            ErrorType.BUSINESS_RULE_VIOLATION,
            ErrorStatus.CONFLICT,
            "비즈니스 규칙을 위반했습니다."
        )

        val DATA_INTEGRITY_ERROR = ErrorInfo(
            ErrorType.DATA_INTEGRITY_ERROR,
            ErrorStatus.INTERNAL_SERVER_ERROR,
            "데이터 무결성 오류가 발생했습니다."
        )

        val ASSIGNMENT_ERROR = ErrorInfo(
            ErrorType.ASSIGNMENT_ERROR,
            ErrorStatus.CONFLICT,
            "배차 처리 중 오류가 발생했습니다."
        )

        val INTERNAL_SERVER_ERROR = ErrorInfo(
            ErrorType.INTERNAL_SERVER_ERROR,
            ErrorStatus.INTERNAL_SERVER_ERROR,
            "서버 내부 오류가 발생했습니다."
        )
    }
} 