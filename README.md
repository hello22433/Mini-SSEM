# 💰 Mini-SSEM : High-Traffic Tax Engine

> **"5월의 폭주하는 트래픽에도 멈추지 않는, 가장 안전한 세금 계산 엔진"**
>
> **경제학 전공자**의 도메인 지식과 **백엔드 개발자**의 대용량 트래픽 처리 기술을 결합하여,
> 널리소프트(SSEM)의 핵심 가치인 '**금융 데이터 정합성**'과 '**시스템 안정성**'을 구현한 프로토타입입니다.

---

## 1. Project Overview (프로젝트 개요)

종합소득세 신고 기간(5월)과 같이 **예측 불가능한 트래픽 폭주 상황**에서도, **서버 다운 없이 안정적으로 세금 계산 요청을 처리**하기 위해 설계되었습니다.

* **Target:** 널리소프트(SSEM) 백엔드 시스템의 핵심 로직 시뮬레이션
* **Core Goal:** 대규모 트래픽 분산 처리, 금융 데이터 무결성 보장, 운영 관측성(Observability) 확보
* **Key Tech:** Spring Boot, RabbitMQ, Redis, Flyway, Bucket4j, Prometheus & Grafana

---

## 2. System Architecture (아키텍처)

*(아키텍처 다이어그램 이미지를 여기에 삽입해주세요)*

1.  **Rate Limiter (Interceptor):** 비정상적인 트래픽 폭주 시, `HandlerInterceptor` 레벨에서 요청을 조기에 차단하여 서버 리소스를 보호합니다.
2.  **Caching (Redis):** 읽기 성능 최적화를 위한 Look-aside Cache 전략을 적용하여 RDBMS 부하를 줄입니다.
3.  **Async Queueing (RabbitMQ):** 사용자의 쓰기(Write) 요청을 큐(Queue)에 적재하여, 워커(Worker)가 처리 가능한 속도로 소비합니다.
4.  **Data Integrity (Transactional):** `BigDecimal`을 통한 정밀 세금 계산 및 `Outbox Pattern`,`DLQ`를 통한 데이터 유실 방지.
5.  **Observability (Monitoring):** Actuator와 Prometheus, Grafana를 연동하여 큐 대기열과 서버 상태를 실시간으로 시각화합니다.

---

## 3. Key Solutions (핵심 문제 해결 전략)

널리소프트의 채용 공고와 기술 블로그에서 파악한 '**실제 운영상의 Pain Point**'를 해결하기 위해 다음과 같은 기술을 도입했습니다.

### 🛡️ 1. 트래픽 제어: "서버 다운을 막는 문지기" (Interceptor & Bucket4j)
* **Problem:** 신고 마감일 등 트래픽 피크 타임에 요청이 몰려 DB 커넥션이 고갈되고 서버가 다운되는 현상.
* **Solution:**
    * **`HandlerInterceptor` + `Bucket4j` 적용:** 컨트롤러 진입 전 인터셉터 단계에서 **Token Bucket 알고리즘**을 통해 초당 허용량(TPS)을 제어합니다.
    * **Effect:** 허용량을 초과하는 요청은 `429 Too Many Requests`로 조기에 거절하여, 핵심 비즈니스 로직과 DB를 보호합니다.
    * **증거자료** :
<img width="612" height="667" alt="스크린샷 2025-12-18 오전 11 43 29" src="https://github.com/user-attachments/assets/af13ed7f-4cbe-413a-a102-9a2838b3588d" />


> **💡 Why Interceptor? (Gateway vs Application Level)**
> 물론 Nginx나 API Gateway 앞단에서 막는 것이 네트워크 리소스 차원에서는 효율적입니다.
> 하지만 저는 **애플리케이션 내부 정책** (**사용자 등급별 제한, 특정 API별 정밀 제어**)과 결합된 유연한 제어가 필요하다고 판단했습니다. 또한, 외부 인프라 의존성을 줄이고 애플리케이션 자체적으로 **Self-Protection** 능력을 갖추기 위해 인터셉터 방식을 선택했습니다.

### ⚡ 2. 성능 최적화: "DB 부하 90% 감소" (Redis Caching)
* **Problem:** 세금 계산 결과 조회(Read) 트래픽이 몰릴 경우 RDBMS 부하가 급증하여 전체 서비스 응답 속도 저하.
* **Solution:**
    * **`Redis` 도입:** 계산 완료(`COMPLETED`)된 불변 데이터는 Redis에 캐싱하는 **Look-aside 전략**을 적용했습니다.
    * **Effect:** 반복되는 조회 요청을 메모리(Redis)에서 1ms 이내에 처리하여 RDBMS 부하를 획기적으로 낮췄습니다.

### 📉 3. 관측 가능성 확보: "보이지 않으면 관리할 수 없다" (Prometheus & Grafana)
* **Problem:** 서비스 지연 시 병목 구간이 DB인지, 큐인지, 애플리케이션인지 파악하기 어려움.
* **Solution:**
    * **Spring Actuator**로 애플리케이션의 메트릭(Metric)을 노출하고, **Prometheus**가 이를 수집합니다.
    * **Grafana Dashboard**를 구축하여 **'현재 대기 중인 신고 요청 수(RabbitMQ Depth)**'와 **'JVM 메모리 상태**'를 실시간으로 모니터링합니다.
    * **Effect:** 장애 발생 시 로그를 뒤지는 대신, 대시보드를 통해 즉각적인 원인 파악 및 대응이 가능해졌습니다.
    * **증거자료** : 
<img width="918" height="840" alt="스크린샷 2025-12-17 오후 10 53 49" src="https://github.com/user-attachments/assets/3aa066b0-fce1-4369-97f1-f24ad20669da" />


### 🏗️ 4. 배포 안정성: "기도 메타는 그만" (Flyway)
* **Problem:** 시스템 고도화 과정에서 DB 스키마 불일치로 인한 배포 장애 발생 가능성.
* **Solution:**
    * **Flyway**를 도입하여 모든 DB 스키마 변경(DDL)을 SQL 스크립트로 버전 관리합니다.
    * JPA의 `ddl-auto`에 의존하지 않고, 마이그레이션 이력을 추적하여 배포 실패 리스크를 최소화했습니다.

### 💰 5. 금융 정합성: "1원의 오차도 허용하지 않음" (Economic Logic)
* **Problem:** 부동소수점 오차 및 복잡한 누진세율 계산 로직의 유지보수 어려움.
* **Solution:**
    * 모든 금전 데이터에 `BigDecimal`을 적용하여 연산 정밀도를 보장했습니다.
    * 매년 개정되는 세법을 유연하게 반영하기 위해(OCP 원칙 준수) **전략 패턴**(**Strategy Pattern**)을 사용하여 `TaxPolicy2024`, `TaxPolicy2025` 등으로 로직을 격리했습니다.

---

## 4. Stability Verification (안정성 검증)

Bucket4j(Rate Limiter)가 실제 트래픽 폭주 상황에서 서버를 보호하는지 검증하기 위해 **k6** 부하 테스트를 수행했습니다.

### 🧪 Load Test Scenario
* **Virtual Users:** 100명 동시 접속 (지속적인 트래픽 공격)
* **Goal:** TPS 한계를 초과하는 요청에 대해 `500 Error`(서버 다운) 대신 `429 Error`(방어)를 반환하는지 검증.

### 📊 Result
<img width="542" height="937" alt="스크린샷 2025-12-17 오후 10 54 41" src="https://github.com/user-attachments/assets/d1f97ce2-c5b6-4a32-b479-20fe9436fdce" />

<img width="582" height="667" alt="스크린샷 2025-12-18 오전 10 42 43" src="https://github.com/user-attachments/assets/f12ebcb4-4b57-42c1-86e7-dd356ea9dc4c" />

* **Total Requests:** 약 9,000건
* **Success (200 OK):** 설정된 TPS 준수
* **Rejected (429 Too Many Requests):** 초과 트래픽 방어 성공
* **Server Error (500):** **0건 (가용성 100% 확보)**

> **결론:** 트래픽 과부하 시에도 Rate Limiter가 정상 작동하여 DB 커넥션 고갈을 방지하고 시스템 안정성을 유지함을 입증했습니다.

---

## 5. Tech Stack & Directory Structure

* **Language:** Java 17
* **Framework:** Spring Boot 3.x, Spring Data JPA
* **Database:** MySQL 8.0, Redis, Flyway
* **Messaging:** RabbitMQ (with Dead Letter Queue)
* **Traffic Control:** Bucket4j, Spring Interceptor
* **Monitoring:** Spring Actuator, Prometheus, Grafana
* **Test:** JUnit5, Mockito, k6 (Load Testing)
* **Infra:** Docker Compose

### 📂 Directory Structure (DDD)
경제학 전공자로서 **세금 계산 로직**(**Domain**)의 순수성을 지키기 위해 도메인과 인프라를 분리했습니다.

```text
src/main/java/com/minissem
├── domain          # [핵심] 비즈니스 로직 (순수 Java, 세법 구현)
├── infra           # [구현] RabbitMQ, JPA, Redis 구현체
├── api             # [외부] Controller (Rate Limiter 적용)
└── global          # [설정] Config, Exception Handler
````

-----

## 6\. How to Run (실행 방법)

복잡한 설치 과정 없이 **Docker Compose**를 통해 인프라를 즉시 구축할 수 있습니다.

### Prerequisites

* Docker & Docker Compose

### Steps

1.  **인프라 실행 (MySQL, RabbitMQ, Redis, Prometheus, Grafana)**
    ```bash
    docker-compose up -d
    ```
2.  **애플리케이션 실행**
    ```bash
    ./gradlew bootRun
    ```
3.  **기능 테스트**
    * 세금 신고 요청: `POST /api/tax/calculate`
    * 결과 조회 (캐싱): `GET /api/tax/{requestId}`
    * 모니터링 대시보드: `http://localhost:3001` (Grafana)

-----

## 📮 Contact

* **Email:** prettywang777@gmail.com
* **Blog:** https://velog.io/@hello22433/posts
* **Note:** 이 프로젝트는 널리소프트의 비전인 "**모두가 최저세금으로**"에 기여할 수 있는 기술적 역량을 증명하기 위해 제작되었습니다.
