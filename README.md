# 물류기사 배차 관리 시스템 - 백엔드

Spring Boot와 Kotlin으로 구현된 물류기사 배차 관리 시스템의 백엔드 API입니다.

## 📝 업데이트 내역 (2024-06)

- 테스트 코드 개선 및 어노테이션/유니크 제약조건 관련 문제 해결
- 기사 월별 매출 API 추가 (`GET /api/drivers/{id}/revenue?year={year}&month={month}`)
- ErrorConstants 및 예외 처리 메시지/상태코드 통일
- 테스트 실행 및 문제 해결 과정 요약

## 🛠 기술 스택

- **Framework**: Spring Boot 3.20 **Language**: Kotlin 10.920
- **Database**: H2(개발용)
- **ORM**: JPA/Hibernate
- **Query DSL**: QueryDSL 5
- **Build Tool**: Gradle
- **Test**: JUnit5MockMvc

## 📋 시스템 요구사항

- Java 17이상
- Gradle

## 🚀 설치 및 실행

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd cow-bob/backend
```

### 2 의존성 설치
```bash
./gradlew build
```

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

서버가 http://localhost:8080서 실행됩니다.

## 📂 프로젝트 구조

```
backend/
├── src/
│   ├── main/
│   │   ├── kotlin/com/logistics/
│   │   │   ├── entity/           # JPA 엔티티
│   │   │   │   ├── Driver.kt
│   │   │   │   ├── Delivery.kt
│   │   │   │   └── Vacation.kt
│   │   │   ├── repository/       # 데이터 저장소
│   │   │   │   ├── DriverRepository.kt
│   │   │   │   ├── DeliveryRepository.kt
│   │   │   │   └── VacationRepository.kt
│   │   │   ├── service/          # 비즈니스 로직
│   │   │   │   ├── DriverService.kt
│   │   │   │   ├── DeliveryService.kt
│   │   │   │   └── VacationService.kt
│   │   │   ├── controller/       # REST API 컨트롤러
│   │   │   │   ├── DriverController.kt
│   │   │   │   ├── DeliveryController.kt
│   │   │   │   └── VacationController.kt
│   │   │   ├── config/           # 설정 클래스
│   │   │   │   └── QuerydslConfig.kt
│   │   │   ├── exception/        # 예외 처리
│   │   │   │   ├── GlobalExceptionHandler.kt
│   │   │   │   ├── ErrorConstants.kt
│   │   │   │   └── CustomExceptions.kt
│   │   │   └── LogisticsApplication.kt
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       └── kotlin/com/logistics/
│           ├── controller/       # 컨트롤러 테스트
│           ├── service/          # 서비스 테스트
│           ├── repository/       # 저장소 테스트
│           └── exception/        # 예외 처리 테스트
├── build.gradle.kts
└── README.md
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
- `GET /api/drivers/{id}/revenue?year={year}&month={month}` - 기사 월별 매출

### 배송 관리
- `GET /api/deliveries` - 배송 목록 조회
- `POST /api/deliveries` - 배송 등록
- `GET /api/deliveries/{id}` - 배송 상세 조회
- `PUT /api/deliveries/{id}` - 배송 수정
- `DELETE /api/deliveries/{id}` - 배송 삭제
- `GET /api/deliveries/pending` - 대기 중인 배송 목록
- `POST /api/deliveries/{id}/recommend-driver` - 배차 추천
- `POST /api/deliveries/{deliveryId}/assign/[object Object]driverId}` - 배차 확정
- `POST /api/deliveries/[object Object]id}/complete` - 배송 완료

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

1. **휴가 일정**: 휴가 중인 기사는 배차에서 제외2. **배송 횟수 균등화**: 총 배송 횟수가 적은 기사 우선
3. **최근 배송 이력**: 최근30일 내 배송 횟수 고려
4. **동일 지역 반복**: 같은 지역 반복 배차 방지

### 점수 계산 방식
```kotlin
점수 = 총배송횟수 + (최근배송횟수 × 2 + (동일지역배송횟수 × 5)
```

점수가 낮을수록 우선순위가 높습니다.

## 🧪 테스트

### 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests DriverControllerTest

# 테스트 커버리지 확인
./gradlew test jacocoTestReport
```

### 테스트 구조
- **Controller Tests**: REST API 엔드포인트 테스트
- **Service Tests**: 비즈니스 로직 테스트
- **Repository Tests**: 데이터 접근 계층 테스트
- **Exception Tests**: 예외 처리 테스트

## 🔐 개발 환경 설정

### H2 데이터베이스 콘솔 접근
개발 중에는 H2 콘솔을 통해 데이터베이스를 확인할 수 있습니다:
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:logistics
- Username: sa
- Password: (비어있음)

### 환경 변수
- `SPRING_PROFILES_ACTIVE`: 활성 프로파일 (dev, test, prod)
- `DB_URL`: 데이터베이스 연결 URL
- `DB_USERNAME`: 데이터베이스 사용자명
- `DB_PASSWORD`: 데이터베이스 비밀번호

## 🚨 예외 처리

시스템은 다음과 같은 예외 상황을 처리합니다:

- **유효성 검사 오류**: 입력 데이터 검증 실패 시 적절한 오류 메시지 반환
- **비즈니스 로직 오류**: 배차 충돌, 중복 데이터 등 비즈니스 규칙 위반 시 처리
- **시스템 오류**: 데이터베이스 연결 실패, 서버 오류 등 시스템 레벨 오류 처리

### 예외 타입
- `DriverException`: 기사 관련 예외
- `DeliveryException`: 배송 관련 예외
- `VacationException`: 휴가 관련 예외
- `AssignmentException`: 배차 관련 예외

## 📊 성능 최적화

### 데이터베이스 최적화
- QueryDSL을 활용한 동적 쿼리 최적화
- 인덱스 설정으로 조회 성능 향상
- 배치 처리로 대량 데이터 처리 최적화

### 캐싱 전략
- 기사 목록 캐싱
- 배송 상태 캐싱
- 휴가 일정 캐싱

## 🔒 보안

### 입력 검증
- 모든 API 입력에 대한 유효성 검사
- SQL Injection 방지
- XSS 공격 방지

### 인증 및 권한
- JWT 토큰 기반 인증 (향후 구현 예정)
- 역할 기반 접근 제어 (향후 구현 예정)

## 📈 모니터링

### 로깅
- SLF4J + Logback을 사용한 구조화된 로깅
- 로그 레벨별 관리
- 에러 로그 집중 모니터링

### 메트릭
- Spring Boot Actuator를 통한 애플리케이션 메트릭 수집
- 커스텀 메트릭 추가 (향후 구현 예정)

## 🤝 기여하기

1. 저장소를 포크합니다
2. 피처 브랜치를 생성합니다 (`git checkout -b feature/amazing-feature`)3 변경사항을 커밋합니다 (`git commit -m 'Add amazing feature'`)
4. 브랜치에 푸시합니다 (`git push origin feature/amazing-feature`)
5. Pull Request를 생성합니다

### 코딩 컨벤션
- Kotlin 코딩 컨벤션 준수
- 함수명은 camelCase 사용
- 클래스명은 PascalCase 사용
- 상수는 UPPER_SNAKE_CASE 사용

## 📄 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

---

**물류기사 배차 관리 시스템 백엔드** - 공정하고 효율적인 배차 관리를 위한 API 서버