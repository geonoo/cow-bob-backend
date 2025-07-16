package com.logistics.exception

// 기본 비즈니스 예외
open class LogisticsException(message: String) : RuntimeException(message)

// 리소스를 찾을 수 없는 경우
class ResourceNotFoundException(message: String) : LogisticsException(message)

// 비즈니스 규칙 위반
class BusinessRuleViolationException(message: String) : LogisticsException(message)

// 잘못된 요청 데이터
class InvalidRequestException(message: String) : LogisticsException(message)

// 배차 관련 예외
class AssignmentException(message: String) : LogisticsException(message)

// 데이터 무결성 위반
class DataIntegrityException(message: String) : LogisticsException(message)