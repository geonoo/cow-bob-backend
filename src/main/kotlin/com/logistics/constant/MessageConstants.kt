package com.logistics.constant

enum class ErrorMessage(val message: String) {
    // 공통 에러
    RESOURCE_NOT_FOUND("리소스를 찾을 수 없습니다."),
    DATA_INTEGRITY_ERROR("데이터 무결성 오류가 발생했습니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    BUSINESS_RULE_VIOLATION("비즈니스 규칙을 위반했습니다."),
    
    // 배송 관련
    DELIVERY_NOT_FOUND("ID가 {id} 인 배송을 찾을 수 없습니다."),
    DELIVERY_CREATE_ERROR("배송 생성 중 오류가 발생했습니다: {message}"),
    DELIVERY_UPDATE_ERROR("배송 수정 중 오류가 발생했습니다: {message}"),
    DELIVERY_DELETE_ERROR("배송 삭제 중 오류가 발생했습니다: {message}"),
    DELIVERY_COMPLETE_ERROR("배송완료 처리 중 오류가 발생했습니다: {message}"),
    DELIVERY_IN_PROGRESS_DELETE("진행 중인 배송은 삭제할 수 없습니다."),
    DELIVERY_ALREADY_PROCESSED("이미 처리된 배송입니다."),
    DELIVERY_NOT_ASSIGNED("배정되지 않은 배송은 완료할 수 없습니다."),    
    // 배송 데이터 검증
    DESTINATION_REQUIRED("배송지는 필수 입력 항목입니다."),
    ADDRESS_REQUIRED("주소는 필수 입력 항목입니다."),
    PRICE_POSITIVE("가격은 0보다 커야 합니다."),
    FEED_TONNAGE_POSITIVE("사료량은 0보다 커야 합니다."),
    DELIVERY_DATE_FUTURE("배송일은 오늘 이후여야 합니다."),
    
    // 기사 관련
    DRIVER_NOT_FOUND("ID가 {id} 인 기사를 찾을 수 없습니다."),
    DRIVER_CREATE_ERROR("기사 생성 중 오류가 발생했습니다: {message}"),
    DRIVER_UPDATE_ERROR("기사 수정 중 오류가 발생했습니다: {message}"),
    DRIVER_DELETE_ERROR("기사 삭제 중 오류가 발생했습니다: {message}"),
    DRIVER_DELIVERY_IN_PROGRESS("배송 중인 기사는 삭제할 수 없습니다."),    
    // 기사 데이터 검증
    DRIVER_NAME_REQUIRED("기사 이름은 필수 입력 항목입니다."),
    PHONE_NUMBER_REQUIRED("전화번호는 필수 입력 항목입니다."),
    PHONE_NUMBER_INVALID("올바른 전화번호 형식이 아닙니다."),
    PHONE_NUMBER_DUPLICATE("이미 등록된 전화번호입니다."),
    VEHICLE_NUMBER_REQUIRED("차량번호는 필수 입력 항목입니다."),
    VEHICLE_TYPE_REQUIRED("차량종류는 필수 입력 항목입니다."),
    TONNAGE_POSITIVE("톤수는 0보다 커야 합니다."),
    JOIN_DATE_PAST("가입일은 오늘 이전이어야 합니다."),
    
    // 배차 관련
    ASSIGNMENT_ERROR("배차 처리 중 오류가 발생했습니다: {message}"),
    DRIVER_NOT_ACTIVE("활성 상태가 아닌 기사에게는 배차할 수 없습니다."),
    DRIVER_TONNAGE_INSUFFICIENT("기사의 차량 톤수({driverTonnage}톤)가 사료량({feedTonnage}톤)보다 작습니다."),
    DRIVER_NOT_AVAILABLE("해당 날짜에 기사가 사용 가능하지 않습니다."),
    NO_AVAILABLE_DRIVER("사용 가능한 기사가 없습니다."),
    
    // 휴가 관련
    VACATION_NOT_FOUND("ID가 {id} 인 휴가를 찾을 수 없습니다."),
    VACATION_CREATE_ERROR("휴가 신청 중 오류가 발생했습니다: {message}"),
    VACATION_UPDATE_ERROR("휴가 수정 중 오류가 발생했습니다: {message}"),
    VACATION_DELETE_ERROR("휴가 삭제 중 오류가 발생했습니다: {message}")
}

enum class SuccessMessage(val message: String) {
    // 공통 성공
    CREATE_SUCCESS("생성되었습니다."),
    UPDATE_SUCCESS("수정되었습니다."),
    DELETE_SUCCESS("삭제되었습니다."),
    
    // 배송 관련
    DELIVERY_CREATED("배송이 생성되었습니다."),
    DELIVERY_UPDATED("배송이 수정되었습니다."),
    DELIVERY_DELETED("배송이 삭제되었습니다."),
    DELIVERY_COMPLETED("배송이 완료되었습니다."),
    DELIVERY_ASSIGNED("배송이 배차되었습니다."),
    
    // 기사 관련
    DRIVER_CREATED("기사가 등록되었습니다."),
    DRIVER_UPDATED("기사 정보가 수정되었습니다."),
    DRIVER_DELETED("기사가 삭제되었습니다."),
    
    // 휴가 관련
    VACATION_CREATED("휴가가 신청되었습니다."),
    VACATION_UPDATED("휴가 정보가 수정되었습니다."),
    VACATION_DELETED("휴가가 삭제되었습니다."),
    VACATION_APPROVED("휴가가 승인되었습니다."),
    VACATION_REJECTED("휴가가 반려되었습니다.")
}

object MessageUtils {

    // 키워드 기반 파라미터 대체 (권장 방식)
    fun formatMessage(message: ErrorMessage, params: Map<String, String>): String {
        var result = message.message
        params.forEach { (key, value) ->
            result = result.replace("{$key}", value)
        }
        return result
    }

    // 단일 키워드 파라미터(id 전용) 대체
    fun formatMessage(message: ErrorMessage, id: String): String {
        return message.message.replace("{id}", id)
    }
}
