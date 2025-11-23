## 웹서비스설계 실습2

간단한 유저 관리 예제로 8개의 REST API(POST/GET/PUT/DELETE 각 2개), 표준 응답 포맷, 예외 처리, 요청 로깅 미들웨어를 담은 Spring Boot 서비스입니다.

### 프로젝트 구성

- 루트 구조
  ```
  prac2/
  ├─ build.gradle          # Gradle 설정 (Spring Boot, Lombok 등 의존성)
  ├─ src/
  │  └─ main/
  │     ├─ java/com/example/prac2/
  │     │  ├─ Prac2Application.java        # 스프링 부트 엔트리 포인트
  │     │  ├─ api/
  │     │  │  ├─ ApiResponse.java          # 표준 응답 래퍼 (status/data/error)
  │     │  │  ├─ UserApiController.java    # 8개 사용자 API 구현
  │     │  │  └─ ApiExceptionHandler.java  # 500 에러 포맷/로그 처리
  │     │  ├─ config/
  │     │  │  └─ RequestLoggingFilter.java # 요청/응답 로깅 미들웨어
  │     │  └─ model/
  │     │     └─ SimpleUser.java           # 데모용 유저 모델
  └─ README.md
  ```

- `src/main/java/com/example/prac2/api/ApiResponse.java`  
  모든 응답을 `status/data/error` 구조로 감싸는 래퍼.
- `src/main/java/com/example/prac2/api/UserApiController.java`  
  인메모리 유저 CRUD와 상태 토글, 유지보수 모드 등 8개 엔드포인트 구현.
- `src/main/java/com/example/prac2/api/ApiExceptionHandler.java`  
  500 에러를 포맷에 맞게 반환하고 로그에 남기는 전역 예외 처리기.
- `src/main/java/com/example/prac2/model/SimpleUser.java`  
  데모용 유저 모델 (id, name, email, active).
- `src/main/java/com/example/prac2/config/RequestLoggingFilter.java`  
  모든 요청/응답을 `METHOD path -> status (ms)` 형식으로 로깅하는 미들웨어.

### 공통 응답 포맷

```json
{
  "status": "success | error",
  "data": { ... },
  "error": "에러 메시지(없으면 null)"
}
```

### 엔드포인트 한눈에 보기

베이스 URL: `http://localhost:8080/api`

- `POST /users` 사용자 생성 (201, 400)
- `POST /users/maintenance` 점검 모드 예시 (503)
- `GET /users` 전체 목록 (200)
- `GET /users/{id}` 단건 조회 (200, 404)
- `PUT /users/{id}` 이름/이메일 수정 (200, 400, 404)
- `PUT /users/{id}/status` 활성/비활성 전환, `"reason":"panic"` 시 500 트리거 (200, 404, 500)
- `DELETE /users/{id}` 단건 삭제 (200, 404)
- `DELETE /users/inactive` 비활성 사용자 일괄 삭제 (200, 404)

### Postman 테스트

1) 기본 세팅  
- 새 요청을 만들고 메서드/URL을 위 엔드포인트에 맞게 설정합니다.  
- `Body -> raw -> JSON` 선택 후 JSON을 입력합니다(POST/PUT).

---

2) 요청별 예시  
### **POST /users**: `{ "name":"Jin", "email":"jin@example.com" }` → 201, 생성된 유저 반환.

![post](/screenshots/post.png)

---

### **POST /users/maintenance**: 항상 503으로 점검 모드 예시.

![postMain](/screenshots/postMaintenance.png)

---
### **GET /users**: 바디 없이 호출 → 200, 유저 리스트.

- 전체 검색

![get](/screenshots/get.png)

---
- 단일 검색

![get1](/screenshots/get1.png)

---

### **PUT /users/{id}**: `{ "name":"Young", "email":"young@example.com" }` → 200. 필수 필드 누락 시 400, 없는 id는 404.  
  
![put1](/screenshots/put1.png)

---

### **PUT /users/{id}/status**: `{ "active":false, "reason":"cleanup" }` → 200. `reason:"panic"` 으로 보내면 500 시나리오 확인.  

![putStatus](/screenshots/putStatus.png)
![putStatusError](/screenshots/putStatusError.png)

---

### **DELETE /users/{id}**: 존재 시 200, 없으면 404.  
![delete2](/screenshots/delete2.png)
![delete5](/screenshots/delete5.png)

---

### **DELETE /users/inactive**: 비활성 유저가 없으면 404, 있으면 200에 삭제 결과.  
![deleteInactive](/screenshots/deleteInactive.png)

---

### **Middleware**
![middleware](/screenshots/middleware.png)

---


3) 응답 확인 포인트  
- Response Body에서 `status` 와 `data` 혹은 `error` 값이 포맷대로 오는지 확인합니다.  
- Status Code가 2xx/4xx/5xx 다양하게 나오는지 확인합니다.

### 미들웨어 로깅 보기

1) 터미널에서 서버 실행: `./gradlew bootRun`  
2) Postman으로 요청을 보낸 뒤 터미널 로그를 확인합니다.  
3) `GET /api/users -> 200 (3 ms)` 형태로 출력되면 `RequestLoggingFilter` 가 정상 동작 중입니다.
