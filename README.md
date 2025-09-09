# 📅 TaskFlow Backend

> TaskFlow는 팀 기반의 작업 관리 플랫폼으로, 효율적인 협업과 프로젝트 관리를 위한 백엔드 API 서비스입니다. 
> 프론트엔드와 연동하여 회원 관리, 작업 관리, 팀 관리, 대시보드 등의 핵심 기능을 제공합니다.
<img width="1528" height="540" alt="image" src="https://github.com/user-attachments/assets/ac6d24c7-152c-4190-a4ba-45ff5b5b0aaf" />

## 데모 링크

**Frontend Demo**: [웹사이트](https://taskflow-ten-tan.vercel.app)

**ERD**: [사진 링크](https://github.com/axios-nbc/taskflow-backend/issues/38)

## 주요 기능

### 사용자 관리 (User Management)
- 회원가입 및 로그인
- 사용자 프로필 관리
- JWT 기반 인증 시스템

### 작업 관리 (Task Management)
- 작업 생성, 조회, 수정, 삭제
- Soft Delete 방식의 안전한 데이터 관리
- 작업 상태 추적 및 관리

### 팀 관리 (Team Management)
- 팀 생성 및 관리
- 팀원 초대 및 권한 관리
- 팀 기반 작업 협업

### 댓글 시스템 (Comment System)
- 작업별 댓글 기능
- 실시간 커뮤니케이션 지원

### 대시보드 & 검색 (Dashboard & Search)
- 통합 검색 기능 (작업/사용자/팀)
- 작업 현황 대시보드
- 데이터 시각화를 위한 API 제공

### 활동 로그 (Activity Log)
- 사용자 활동 추적
- 시스템 로그 관리

## Built With

![Java 17](https://img.shields.io/badge/Java-17-orange?logo=openjdk&logoColor=white) 
[![Spring Boot 3.5.5](https://img.shields.io/badge/Spring%20Boot-3.5.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot) 
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-6DB33F?logo=spring&logoColor=white) 
![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white) 
![H2 Database](https://img.shields.io/badge/H2%20Database-0078D4?logo=h2&logoColor=white)
![Lombok](https://img.shields.io/badge/Lombok-ED1C24?logo=java&logoColor=white) 
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?logo=springsecurity&logoColor=white) 
![Spring Boot Validation](https://img.shields.io/badge/Validation-Spring%20Boot%20Starter-6DB33F?logo=spring&logoColor=white) 
![JUnit](https://img.shields.io/badge/JUnit-Platform-25A162?logo=junit5&logoColor=white) 
![Mockito](https://img.shields.io/badge/Mockito-5.8.0-yellow?logo=java&logoColor=white) 
![BCrypt](https://img.shields.io/badge/BCrypt-0.10.2-blue?logo=java&logoColor=white) 
![JWT](https://img.shields.io/badge/JWT-0.11.5-000000?logo=jsonwebtokens&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?logo=gradle&logoColor=white)

---

## 📁 프로젝트 구조

```
taskflow-backend/
├── src/
│   ├── main/
│   │   ├── java/org/example/taskflowd/
│   │   │   ├── common/
│   │   │   │   ├── annotation/
│   │   │   │   ├── config/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── enums/
│   │   │   │   ├── exception/
│   │   │   │   ├── security/
│   │   │   │   └── util/
│   │   │   ├── domain/
│   │   │   │   ├── activityLog/
│   │   │   │   ├── comment/
│   │   │   │   ├── dashboard/
│   │   │   │   ├── search/
│   │   │   │   ├── task/
│   │   │   │   ├── team/
│   │   │   │   └── user/
│   │   │   └── TaskflowDApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   └── test/
```

## API 명세

### 주요 도메인별 API

| 도메인 | 기능 | 설명 |
|--------|------|------|
| **User** | 회원가입 / 로그인 / 프로필 페이지 연동 | 사용자 인증 및 프로필 관리 |
| **Task** | Task 조회 / 생성 / 수정 / 삭제(soft delete) | 작업 생명주기 관리 |
| **Comment** | 댓글 작성 / 조회 / 수정 / 삭제 | 작업별 댓글 시스템 |
| **Team** | 팀 생성 / 조회 / 수정 / 삭제 / 멤버 관리 | 팀 및 멤버십 관리 |
| **Dashboard** | 작업 / 유저 / 팀 현황 대시보드 | 통합 대시보드 및 통계 |
| **Search** | 통합 검색 (작업/사용자/팀) | 전체 검색 및 필터링 기능 |
| **ActivityLog** | 사용자 활동 추적 및 로그 관리 | 시스템 활동 기록 및 감사 |

### API 통신 방식
- **JSON 기반 REST API**: 프론트엔드와의 표준화된 통신
- **요청/응답 DTO**: 데이터 전송 최적화
- **입력 데이터 검증(Validation)**: 안전한 데이터 처리

## 시작하기

### 사전 요구사항

- Java 17 이상
- MySQL 8.0 이상 (운영환경)
- H2 Database (개발환경)
- Gradle

### 설치 및 실행

1. **저장소 클론**
   ```bash
   git clone https://github.com/axios-nbc/taskflow-backend.git
   cd taskflow-backend
   ```

2. **데이터베이스 설정**
   ```yaml
   # application.yml 설정
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/taskflow
       username: your_username
       password: your_password
   ```

3. **애플리케이션 실행**
   ```bash
   # Gradle 사용시
   ./gradlew bootRun
   ```

4. **개발환경 실행 (H2 Database)**
   ```bash
   # H2 콘솔: http://localhost:8080/h2-console
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

## 테스트

```bash
# 단위 테스트 실행
./gradlew test

# 통합 테스트 포함
./gradlew check

# 테스트 커버리지 확인
./gradlew jacocoTestReport
```

## 보안 기능

- **JWT 기반 인증**: 상태 비저장 인증 시스템
- **BCrypt 암호화**: 안전한 비밀번호 해싱
- **Spring Security**: 종합적인 보안 프레임워크
- **입력 검증**: 악성 데이터 차단

## 모니터링 및 로깅

- **Spring Boot Actuator**: 애플리케이션 헬스 체크
- **활동 로그**: 사용자 행동 추적
- **에러 핸들링**: 체계적인 예외 처리

## 기여하기

이 프로젝트에 기여하고 싶으시다면, 다음 가이드라인을 따라주세요:

- 저장소를 포크(Fork)합니다.
- 새로운 기능 또는 버그 수정을 위한 브랜치를 생성합니다 (`git checkout -b feature/your-feature-name`).
- 변경 사항을 커밋하고 푸시합니다.
- Pull Request를 생성하여 변경 사항을 설명합니다.

## 라이센스

이 프로젝트는 MIT 라이센스를 따릅니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 문의

프로젝트 관련 문의사항이 있으시면 [Issues](https://github.com/axios-nbc/taskflow-backend/issues) 페이지를 이용해주세요.

---

<div align="center">
  <sub>Built with ❤️ by Axios Team</sub>
</div>
