# 수강신청 백엔드

Spring Boot 기반 수강신청 백엔드 프로젝트입니다. 단순 CRUD 수강신청에서 출발해, 여러 사용자가 동시에 몰리는 상황에서 정원 초과를 막고, 로그인 이후 메인 페이지 진입을 Redis 대기열로 제어하는 방향으로 고도화했습니다. 이 프로젝트의 주요 목표는 대기열 구현 및 학습입니다.

이 프로젝트에서 특히 집중한 부분은 다음 세 가지입니다.

- 수강신청 정원 초과를 막기 위한 DB 조건부 UPDATE
- Redis Sorted Set 기반 로그인 대기열과 입장권 TTL 관리
- k6 부하 테스트를 통한 polling 병목 확인과 adaptive polling 개선

## 기술 스택

| 구분 | 사용 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 4.0.5 |
| Web | Spring Web MVC |
| Security | Spring Security, JWT, BCrypt |
| Database | Oracle |
| Persistence | MyBatis, 일부 JPA 의존성 |
| Cache / Queue | Redis, Spring Data Redis |
| Monitoring | Spring Boot Actuator |
| Load Test | k6 |
| Build | Gradle |

## 주요 기능

### 인증

- 이메일/비밀번호 기반 로그인
- BCrypt를 이용한 비밀번호 암호화
- JWT access token 발급
- Spring Security filter에서 JWT 검증 후 `Authentication` 구성
- 서버 세션을 사용하지 않는 stateless 구조

### 강의 조회와 수강신청

- 강의 목록 조회
- 수강신청 등록
- 수강신청 내역 조회
- 수강신청 취소
- 중복 신청 방지
- 정원 초과 방지

### Redis 로그인 대기열

로그인 이후 모든 사용자를 바로 메인 화면으로 보내지 않고 Redis 대기열을 거치게 했습니다.

사용한 Redis key 구조는 다음과 같습니다.

```text
queue:login
-> 대기 중인 사용자 목록
-> Redis Sorted Set
-> score: 대기열 진입 시간
-> member: 사용자 email

active:login
-> 현재 메인 페이지에 입장한 사용자 목록
-> Redis Sorted Set
-> score: 입장 만료 시간
-> member: 사용자 email

ticket:{email}
-> 메인 페이지 접근 권한
-> Redis String + TTL
```

현재 구현 기준으로 동시에 메인 화면에 머무를 수 있는 사용자는 3명으로 제한했습니다.

```java
private static final long MAX_ACTIVE_USERS = 3;
private static final long TICKET_TTL_MINUTES = 5;
```

대기열 흐름은 다음과 같습니다.

```text
1. 사용자가 로그인한다.
2. /api/queue/enter 호출로 queue:login에 들어간다.
3. /api/queue/status로 자신의 rank, waitingAhead, allowed를 확인한다.
4. allowed=true가 되면 /api/queue/admit을 호출한다.
5. admit 성공 시 queue:login에서 제거되고 active:login에 들어간다.
6. ticket:{email}이 TTL과 함께 발급된다.
7. 사용자가 메인 화면에 진입한다.
8. 사용자가 나가거나 TTL이 만료되면 active/ticket에서 제거된다.
```

## API 개요

| Method | Endpoint | 설명 |
| --- | --- | --- |
| POST | `/api/auth/signUp` | 회원가입 |
| POST | `/api/auth/login` | 로그인 및 JWT 발급 |
| GET | `/api/auth` | 로그인 사용자 정보 조회 |
| GET | `/api/lecture` | 강의 목록 조회 |
| POST | `/api/registration/{lectureNo}` | 수강신청 |
| GET | `/api/registration` | 수강신청 내역 조회 |
| DELETE | `/api/registration/{registrationNo}` | 수강신청 취소 |
| POST | `/api/queue/enter` | 로그인 대기열 진입 |
| GET | `/api/queue/status` | 대기열 상태 조회 |
| POST | `/api/queue/admit` | 입장 시도 |
| GET | `/api/queue/ticket` | 입장권 확인 |
| DELETE | `/api/queue/leave` | 대기열/활성 사용자 퇴장 처리 |
| GET | `/actuator/health` | 서버 상태 확인 |
| GET | `/actuator/metrics/jvm.threads.live` | JVM live thread 수 확인 |
| GET | `/actuator/threaddump` | thread dump 확인 |

## 설계 포인트 1. 정원 초과 방지

처음에는 수강신청 시 강의의 현재 수강 인원을 단순히 증가시키는 방식으로 접근했습니다.

```sql
update lecture
set currentEnrollment = currentEnrollment + 1
where lecture_no = #{lectureNo}
```

하지만 수강신청처럼 여러 사용자가 같은 강의를 거의 동시에 신청하는 상황에서는 이 방식만으로는 정원 초과 가능성이 있습니다.

문제는 다음 흐름입니다.

```text
1. 현재 인원 조회
2. 정원보다 작은지 Java 코드에서 판단
3. 현재 인원 + 1 update
```

1번과 3번 사이에 다른 요청이 끼어들 수 있기 때문에, 정원 확인과 증가를 분리하면 동시성 문제가 생길 수 있습니다.

그래서 정원 확인과 증가를 하나의 SQL UPDATE 문으로 묶었습니다.

```xml
<update id="checkCapacityAndIncreaseCurrentEnrollment">
    update lecture
    set currentEnrollment = currentEnrollment + 1
    where lecture_no = #{lectureNo}
      and currentEnrollment &lt; capacity
</update>
```

MyBatis update 결과는 영향을 받은 row 수로 판단합니다.

```text
updated = 1
-> 정원이 남아 있어 증가 성공

updated = 0
-> 정원이 꽉 차서 증가 실패
```

서비스 로직에서는 이 결과를 기준으로 수강신청 성공/정원 초과를 나눕니다.

```java
int updated = lectureMapper.checkCapacityAndIncreaseCurrentEnrollment(lectureNo);

if (updated == 0) {
    return FULL;
}

registrationMapper.registerLecture(member.getMemberNo(), lectureNo);
```

수강 인원 증가와 registration insert는 하나의 작업 단위로 묶여야 하므로 `@Transactional`을 적용했습니다.

## 설계 포인트 2. Redis를 사용한 이유

대기열과 활성 사용자는 영구 보관이 중요한 데이터가 아닙니다.

```text
queue:login
-> 지금 기다리는 사용자

active:login
-> 지금 입장 중인 사용자

ticket:{email}
-> 일정 시간 동안만 유효한 접근 권한
```

이 값들은 다음 특징을 가집니다.

- 빠르게 조회되어야 한다.
- 시간이 지나면 자동으로 사라져도 된다.
- 서버 재시작 후 영구 이력으로 남길 필요는 크지 않다.
- DB에 저장하면 쓰기/삭제가 지나치게 빈번해질 수 있다.

그래서 관계형 DB보다 Redis가 더 잘 맞는다고 판단했습니다.

특히 Sorted Set을 사용하면 score를 기준으로 순서를 유지하면서도 rank 조회가 가능합니다.

```text
ZADD queue:login <timestamp> <email>
ZRANK queue:login <email>
ZCARD active:login
ZREMRANGEBYSCORE active:login 0 <now>
```

`active:login`은 score에 만료 시간을 넣어두고, 주기적으로 만료된 사용자를 제거합니다.

```java
@Scheduled(fixedRate = 10000)
public void cleanupExpiredActiveUsers() {
    removeExpiredActiveUsers();
}
```

## 설계 포인트 3. polling 병목과 adaptive polling

처음 대기열은 모든 사용자가 2초마다 `/api/queue/status`를 호출하는 구조였습니다.

```text
모든 대기자
-> 2초마다 status API 호출
```

이 방식은 구현은 단순하지만, 대기자가 많아지면 서버와 Redis에 반복 조회 부하를 만듭니다.

예를 들어 5000명이 모두 2초마다 polling하면 이론적으로 초당 약 2500번의 status 요청이 발생합니다.

```text
5000명 / 2초 = 초당 약 2500 status 요청
```

실제로 k6로 5000명 부하 테스트를 수행했을 때 대부분의 HTTP 요청이 status polling에서 발생했습니다.

### 1차 테스트: 2초 polling

| 항목 | 결과 |
| --- | ---: |
| login_success_total | 5000 |
| enter_queue_success_total | 5000 |
| queue_poll_total | 811,332 |
| http_reqs | 821,935 |
| http_req_failed | 0.00% |
| http_req_duration p95 | 232.5ms |
| data_received | 376 MB |
| data_sent | 231 MB |

테스트는 실패하지 않았지만, 실제 병목은 입장 처리보다 대기 상태 확인 요청이라는 점을 확인했습니다.

그래서 서버가 사용자의 대기 순번을 보고 다음 polling 주기를 내려주는 방식으로 변경했습니다.

```json
{
  "rank": 120,
  "waitingAhead": 119,
  "allowed": false,
  "nextPollMillis": 10000
}
```

현재 정책은 다음과 같습니다.

```text
입장 가능
-> 즉시 admit 시도

앞에 50명 이하
-> 2초 후 다시 status 확인

그 외
-> 10초 후 다시 status 확인
```

프론트와 k6는 서버가 내려준 `nextPollMillis`만 보고 다음 요청 시점을 결정합니다. 이렇게 하면 polling 정책을 프론트에 고정하지 않고 서버에서 조정할 수 있습니다.

### 2차 테스트: adaptive polling

| 항목 | 2초 polling | adaptive polling | 변화 |
| --- | ---: | ---: | ---: |
| queue_poll_total | 811,332 | 175,559 | 약 78% 감소 |
| http_reqs | 821,935 | 186,208 | 약 77% 감소 |
| http_req_duration p95 | 232.5ms | 32.79ms | 개선 |
| data_received | 376 MB | 86 MB | 약 77% 감소 |
| data_sent | 231 MB | 52 MB | 약 77% 감소 |
| http_req_failed | 0.00% | 0.00% | 실패 없음 |

중요한 점은 입장 속도를 빠르게 만든 것이 아니라, 대기 중 발생하는 불필요한 status 요청 수와 서버 부하를 줄였다는 것입니다.

대기 시간은 입장 허용 인원과 체류 시간에 의해 결정됩니다.

```text
MAX_ACTIVE_USERS = 3
STAY_SECONDS = 10
```

따라서 같은 조건에서는 평균 대기 시간이 크게 달라지지 않는 것이 정상입니다. 개선 대상은 대기 시간 자체가 아니라 polling 요청량이었습니다.

## k6 부하 테스트

테스트 스크립트는 `k6` 폴더에 두었습니다.

| 파일 | 목적 |
| --- | --- |
| `queue-smoke-test.js` | 대기열 기본 흐름 확인 |
| `queue-admit-race-test.js` | 동시 admit 경쟁 상황 확인 |
| `queue-random-200-test.js` | 200명 랜덤 진입 테스트 |
| `queue-5000-15m-flow-test.js` | 5000명 장시간 유입 시나리오 |
| `queue-5000-adaptive-polling-test.js` | adaptive polling 부하 테스트 |
| `signup-users-201-to-10000.js` | 테스트 계정 대량 생성 |

예시 실행:

```powershell
k6 run k6\queue-5000-adaptive-polling-test.js
```

환경변수로 테스트 규모를 조정할 수 있습니다.

```powershell
$env:USER_COUNT=5000
$env:ARRIVAL_WINDOW_SECONDS=60
$env:STAY_SECONDS=10
k6 run k6\queue-5000-adaptive-polling-test.js
```

## Actuator로 확인한 것

부하 테스트 중에는 단순히 k6 결과만 보지 않고 Spring 내부 상태도 확인했습니다.

사용한 Actuator endpoint는 다음과 같습니다.

```text
/actuator/health
/actuator/metrics/jvm.threads.live
/actuator/threaddump
```

특히 thread dump에서 다음 스레드들을 구분해서 보았습니다.

```text
http-nio-8080-exec-*
-> Tomcat HTTP 요청 처리 스레드

HikariPool-*:housekeeper
-> DB 커넥션 풀 관리 스레드

lettuce-eventExecutorLoop / lettuce-nioEventLoop
-> Redis 클라이언트 관련 스레드

scheduling-1
-> @Scheduled 작업 실행 스레드
```

thread dump는 과거 로그가 아니라 특정 순간의 스냅샷입니다. 따라서 `http-nio-8080-exec-*` 스레드의 stackTrace에 Controller나 Service가 보이면, 그 순간 해당 요청 처리 스레드가 애플리케이션 코드를 실행 중이라고 해석했습니다.

## 트러블슈팅 요약

### 1. 수강신청 정원 초과 가능성

| 단계 | 내용 |
| --- | --- |
| 문제 정의 | 현재 인원 조회와 증가를 분리하면 동시 요청에서 정원 초과 가능성이 있다. |
| 가설 | 정원 확인과 증가를 하나의 DB UPDATE로 묶으면 경쟁 상황을 줄일 수 있다. |
| 액션 | `where currentEnrollment < capacity` 조건부 UPDATE 적용 |
| 결과 | update row count로 성공/실패를 판단하고, 정원 초과 시 `FULL` 결과 반환 |

### 2. 대기열 polling 부하

| 단계 | 내용 |
| --- | --- |
| 문제 정의 | 5000명 테스트에서 HTTP 요청 대부분이 `/api/queue/status` polling으로 발생했다. |
| 가설 | 모든 사용자가 같은 2초 주기로 polling하는 구조가 비효율적이다. |
| 액션 | 서버가 `nextPollMillis`를 내려주는 adaptive polling 적용 |
| 결과 | status polling 약 78%, 전체 HTTP 요청 약 77% 감소 |

### 3. Redis key와 member 삭제 구분

Redis를 다루면서 key 자체 삭제와 Sorted Set member 삭제를 구분했습니다.

```text
DEL active:login
-> active:login key 전체 삭제

ZREM active:login user@test.com
-> active:login 안의 특정 member만 삭제

ZREMRANGEBYSCORE active:login 0 now
-> 만료 시간이 지난 member들만 삭제
```

`active:login` 전체에 TTL을 거는 방식은 모든 활성 사용자를 동시에 날릴 수 있기 때문에, 각 member의 score에 만료 시간을 넣고 range 삭제를 사용하는 방식으로 정리했습니다.

### 4. jar 실행과 Java PATH 문제

IntelliJ 없이 Spring Boot jar로 실행하려고 할 때 PowerShell에서 `java` 명령을 찾지 못하는 문제가 있었습니다.

```text
java : 'java' 용어가 cmdlet, 함수, 스크립트 파일 또는 실행할 수 있는 프로그램 이름으로 인식되지 않습니다.
```

원인은 `JAVA_HOME`만 설정되어 있고, 실제 `java.exe`가 있는 `bin` 경로가 `Path`에 제대로 잡히지 않은 것이었습니다.

해결은 Java bin 경로를 Path에 직접 추가하는 방식이었습니다.

```text
C:\SpringBootProject\zulu17\bin
```

### 5. VirtualBox 병목 확인

부하 테스트 중 VirtualBox Ubuntu에서 다음 로그를 확인했습니다.

```text
rcu_preempt detected stalls on CPUs/tasks
```

이는 VM이 할당받은 CPU 시간을 제때 얻지 못하거나, 호스트 PC 부하로 인해 Linux kernel task가 오래 응답하지 못할 때 볼 수 있는 메시지입니다.

Redis가 VM 안에서 실행 중이라면 Spring 서버의 Redis 요청도 VM 상태의 영향을 받을 수 있습니다. 그래서 큰 부하 테스트에서는 애플리케이션 코드뿐 아니라 테스트 환경 자체가 병목이 될 수 있다는 점도 함께 확인했습니다.

## 실행 방법

### 1. 의존 서비스 준비

로컬 실행을 위해 Oracle DB와 Redis가 필요합니다.

`src/main/resources/application.properties`에서 다음 값을 환경에 맞게 설정합니다.

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/xepdb1
spring.datasource.username=<DB_USER>
spring.datasource.password=<DB_PASSWORD>

spring.data.redis.host=<REDIS_HOST>
spring.data.redis.port=6379

jwt.secret=<JWT_SECRET>
jwt.expiration=3600000
```

실제 운영 환경에서는 DB 비밀번호, Redis 주소, JWT secret을 코드에 직접 두지 않고 환경변수나 외부 설정으로 분리하는 것이 좋습니다.

### 2. Spring Boot 실행

```powershell
.\gradlew.bat bootRun
```

또는 jar 빌드 후 실행합니다.

```powershell
.\gradlew.bat bootJar
java -jar build\libs\course-0.0.1-SNAPSHOT.jar
```

### 3. 상태 확인

```powershell
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics/jvm.threads.live
```

## 배운 점

이 프로젝트에서 가장 크게 배운 점은 기능 구현 이후 실제 상황을 가정해 검증해야 한다는 것입니다.

처음에는 대기열이 동작하는지만 확인하면 된다고 생각했지만, 5000명 부하 테스트를 돌려보니 문제는 실패 여부가 아니라 요청의 분포였습니다. 서버가 죽지 않아도, 대부분의 요청이 status polling에 쏠린다면 구조적으로 비효율적인 설계일 수 있습니다.

그래서 다음 순서로 접근했습니다.

```text
문제 정의
-> 모든 사용자가 같은 주기로 polling한다.

가설 수립
-> 앞 순번과 먼 순번의 polling 주기를 다르게 하면 요청량이 줄어들 것이다.

액션
-> 서버가 nextPollMillis를 내려주고 클라이언트는 그 값에 맞춰 다음 요청을 보낸다.

검증
-> k6 결과에서 queue_poll_total, http_reqs, p95 응답 시간, 네트워크 사용량을 비교한다.
```

단순히 Redis를 사용했다는 점보다, Redis로 만든 구조가 실제 부하에서 어떤 요청 패턴을 만드는지 확인하고 개선했다는 점에 의미를 두었습니다.

## 앞으로 개선할 점

- 보호 API에 queue ticket 검증 필터 추가
- localStorage 기반 ticket 확인 대신 HTTP-only cookie 또는 signed token 검토
- Redis admit 로직을 Lua script로 묶어 원자성 강화
- SSE 또는 WebSocket 기반 입장 알림 방식 검토
- 테스트 코드 보강
- 운영용 profile 분리와 secret 외부화
- Docker Compose로 Spring, Oracle 또는 대체 DB, Redis 실행 환경 구성
