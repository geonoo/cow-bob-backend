package com.logistics.exception

import java.time.LocalDateTime
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

data class ErrorResponse(
        val timestamp: LocalDateTime = LocalDateTime.now(),
        val status: Int,
        val error: String,
        val message: String,
        val path: String
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
            ex: ResourceNotFoundException,
            request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
                ErrorResponse(
                        status = HttpStatus.NOT_FOUND.value(),
                        error = "Resource Not Found",
                        message = ex.message ?: "요청한 리소스를 찾을 수 없습니다.",
                        path = request.getDescription(false).removePrefix("uri=")
                )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolationException(
            ex: BusinessRuleViolationException,
            request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
                ErrorResponse(
                        status = HttpStatus.BAD_REQUEST.value(),
                        error = "Business Rule Violation",
                        message = ex.message ?: "비즈니스 규칙을 위반했습니다.",
                        path = request.getDescription(false).removePrefix("uri=")
                )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequestException(
            ex: InvalidRequestException,
            request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
                ErrorResponse(
                        status = HttpStatus.BAD_REQUEST.value(),
                        error = "Invalid Request",
                        message = ex.message ?: "잘못된 요청입니다.",
                        path = request.getDescription(false).removePrefix("uri=")
                )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(AssignmentException::class)
    fun handleAssignmentException(
            ex: AssignmentException,
            request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
                ErrorResponse(
                        status = HttpStatus.CONFLICT.value(),
                        error = "Assignment Error",
                        message = ex.message ?: "배차 처리 중 오류가 발생했습니다.",
                        path = request.getDescription(false).removePrefix("uri=")
                )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(DataIntegrityException::class)
    fun handleDataIntegrityException(
            ex: DataIntegrityException,
            request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse =
                ErrorResponse(
                        status = HttpStatus.CONFLICT.value(),
                        error = "Data Integrity Error",
                        message = ex.message ?: "데이터 무결성 오류가 발생했습니다.",
                        path = request.getDescription(false).removePrefix("uri=")
                )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorResponse =
                ErrorResponse(
                        status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        error = "Internal Server Error",
                        message = "서버 내부 오류가 발생했습니다.",
                        path = request.getDescription(false).removePrefix("uri=")
                )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}
