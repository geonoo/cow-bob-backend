package com.logistics.exception

import com.logistics.constant.ErrorInfo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return createErrorResponse(
            ErrorInfo.RESOURCE_NOT_FOUND,
            ex.message,
            request
        )
    }

    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequestException(ex: InvalidRequestException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return createErrorResponse(
            ErrorInfo.INVALID_REQUEST,
            ex.message,
            request
        )
    }

    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolationException(ex: BusinessRuleViolationException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return createErrorResponse(
            ErrorInfo.BUSINESS_RULE_VIOLATION,
            ex.message,
            request
        )
    }

    @ExceptionHandler(DataIntegrityException::class)
    fun handleDataIntegrityException(ex: DataIntegrityException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return createErrorResponse(
            ErrorInfo.DATA_INTEGRITY_ERROR,
            ex.message,
            request
        )
    }

    @ExceptionHandler(AssignmentException::class)
    fun handleAssignmentException(ex: AssignmentException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return createErrorResponse(
            ErrorInfo.ASSIGNMENT_ERROR,
            ex.message,
            request
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        return createErrorResponse(
            ErrorInfo.INTERNAL_SERVER_ERROR,
            null,
            request
        )
    }

    private fun createErrorResponse(
        errorInfo: ErrorInfo,
        message: String?,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            timestamp = LocalDateTime.now(),
            status = errorInfo.status.status.value(),
            error = errorInfo.type.value,
            message = message ?: errorInfo.defaultMessage,
            path = request.getDescription(false)
        )
        return ResponseEntity.status(errorInfo.status.status).body(errorResponse)
    }
}

data class ErrorResponse(
    val timestamp: LocalDateTime,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
