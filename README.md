# 물류기사 배차 관리 시스템 - 백엔드

물류기사들을 위한 공정한 배차 관리 시스템의 백엔드 API 서버입니다.

## 🛠 기술 스택

- **Framework**: Spring Boot 3.2.0
- **Language**: Kotlin 1.9.20
- **Database**: H2 (파일 기반)
- **ORM**: JPA/Hibernate
- **Build Tool**: Gradle with Kotlin DSL
- **Java Version**: 17+

## 🚀 실행 방법

### 1. 사전 요구사항
- Java 17 이상
- Gradle (또는 Gradle Wrapper 사용)

### 2. 프로젝트 클론
```bash
git clone <repository-url>
cd cow-bob-backend
```

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

서버가 http://localhost:8080 에서 실행됩니다.

### 4. H2 데이터베이스 콘솔 접근
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/logistics;AUTO_SERVER=TRUE`
- Username: `sa`
- Password: (비어있음)

## 📂 프로젝트 구조

```
src/main/kotlin/com/logistics/
├── LogisticsApplication.kt    # 메인 애플리케이션
├── controller/                # REST API 컨트롤러
│   ├── DeliveryController.kt
│   ├── DriverController.kt
│   └── VacationController.kt
├── entity/                    # JPA 엔티티
│   ├── Delivery.kt
│   ├── Driver.kt
│   └── Vacation.kt
├── repository/                # 데이터 접근 계층
│   ├── DeliveryRepository.kt
│   ├── DriverRepository.kt
│   └── VacationRepository.kt
└── service/                   # 비즈니스 로직
    ├── DeliveryService.kt
    ├── DriverService.kt
    └── VacationService.kt
```

## 🌐 API 엔드포인트

### 기사 관리
- `GET /api/drivers` - 기사 목록 조회
- `POST /api/drivers` - 기사 등록
- `GET /api/drivers/{id}` - 기사 상세 조회
- `PUT /api/drivers/{id}` - 기사 정보 수정
- `DELETE /api/drivers/{id}` - 기사 삭제
- `GET /api/drivers/active` - 활성 기사 목록
- `GET /api/drivers/available?date={date}` - 특정 날짜 사용 가능한 기사

### 배송 관리
- `GET /api/deliveries` - 배송 목록 조회
- `POST /api/deliveries` - 배송 등록
- `GET /api/deliveries/{id}` - 배송 상세 조회
- `PUT /api/deliveries/{id}` - 배송 수정
- `DELETE /api/deliveries/{id}` - 배송 삭제
- `GET /api/deliveries/pending` - 대기 중인 배송 목록
- `POST /api/deliveries/{id}/recommend-driver` - 배차 추천
- `POST /api/deliveries/{deliveryId}/assign/{driverId}` - 배차 확정
- `POST /api/deliveries/{id}/complete` - 배송 완료

### 휴가 관리
- `GET /api/vacations` - 휴가 목록 조회
- `POST /api/vacations` - 휴가 신청
- `GET /api/vacations/{id}` - 휴가 상세 조회
- `PUT /api/vacations/{id}` - 휴가 수정
- `DELETE /api/vacations/{id}` - 휴가 삭제
- `GET /api/vacations/driver/{driverId}` - 기사별 휴가 목록
- `POST /api/vacations/{id}/approve` - 휴가 승인
- `POST /api/vacations/{id}/reject` - 휴가 반려

## 🔧 핵심 알고리즘

### 공정한 배차 분배 알고리즘

배차 시스템은 다음 요소들을 고려하여 공정한 분배를 수행합니다:

1. **휴가 일정**: 휴가 중인 기사는 배차에서 제외
2. **배송 횟수 균등화**: 총 배송 횟수가 적은 기사 우선
3. **최근 배송 이력**: 최근 30일 내 배송 횟수 고려
4. **동일 지역 반복**: 같은 지역 반복 배차 방지

### 점수 계산 방식
```kotlin
점수 = 총배송횟수 + (최근배송횟수 × 2) + (동일지역배송횟수 × 5)
```

점수가 낮을수록 우선순위가 높습니다.

## 💾 데이터베이스

- **타입**: H2 파일 기반 데이터베이스
- **위치**: `./data/logistics.mv.db`
- **특징**: 
  - 애플리케이션 재시작 후에도 데이터 유지
  - 다른 PC에서 실행 시 기존 데이터 사용 가능
  - Git에 데이터 파일 포함 (lock 파일은 제외)

## 🔧 개발 명령어

```bash
# 애플리케이션 실행
./gradlew bootRun

# 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 클린 빌드
./gradlew clean build
```

## 📄 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.