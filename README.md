# 📘 Mini-SSEM : High-Traffic Tax Engine (비동기 세금 처리 엔진)

> **"5월의 폭주하는 트래픽에도 멈추지 않는, 가장 안전한 세금 계산기"**
>
> **경제학 전공자**의 도메인 지식과 **백엔드 개발자**의 대용량 처리 기술을 결합하여, 널리소프트(SSEM)의 핵심 가치를 기술적으로 구현한 프로토타입입니다.

## 1\. Project Overview (프로젝트 개요)

이 프로젝트는 종합소득세 신고 기간(5월)과 같이 트래픽이 예측 불가능하게 폭주하는 상황에서도, **서버 다운 없이 안정적으로 세금 계산 요청을 처리**하기 위해 설계되었습니다.

* **핵심 목표:** 대규모 트래픽 분산 처리 및 금융 데이터의 무결성 보장
* **주요 기술:** Spring Boot, RabbitMQ, Flyway, Bucket4j, Prometheus & Grafana

## 2\. Architecture (아키텍처)

1.  **Rate Limiter (Interceptor):** 비정상적인 트래픽 폭주 시, `HandlerInterceptor` 레벨에서 요청을 조기에 차단하여 서버 리소스를 보호합니다.
2.  **Async Queueing (RabbitMQ):** 사용자의 요청을 즉시 처리하지 않고 큐(Queue)에 적재하여, 서버가 처리 가능한 속도로 소비(Consume)합니다.
3.  **Data Integrity (Transactional):** `BigDecimal`을 통한 정밀 세금 계산 및 `Outbox Pattern`/`DLQ`를 통한 데이터 유실 방지.
4.  **Observability (Monitoring):** Actuator와 Prometheus, Grafana를 연동하여 큐 대기열과 서버 상태를 실시간으로 시각화합니다.

-----

## 3\. Key Solutions (핵심 문제 해결 전략)

### 🛡️ 1. 트래픽 제어: "서버 다운을 막는 문지기" (Interceptor & Bucket4j)

* **Problem:** 신고 마감일 등 트래픽 피크 타임에 요청이 몰려 DB 커넥션이 고갈되고 서버가 다운되는 현상.
* **Solution:**
    * **`HandlerInterceptor` 적용:** 컨트롤러 진입 전, 인터셉터 단계에서 `Bucket4j`를 사용해 **Token Bucket 알고리즘** 기반의 처리율 제한(Rate Limiting)을 구현했습니다.
    * **Effect:** 초당 허용량(TPS)을 초과하는 요청은 `429 Too Many Requests`로 즉시 거절하여, 핵심 비즈니스 로직과 DB를 보호합니다.

### 📉 2. 관측 가능성 확보: "보이지 않으면 관리할 수 없다" (Prometheus & Grafana)

* **Problem:** 서비스가 느려질 때, 병목 구간이 DB인지 큐인지 파악하기 어려움.
* **Solution:**
    * **Spring Actuator**로 애플리케이션의 메트릭(Metric)을 노출하고, **Prometheus**가 이를 수집(Scraping)합니다.
    * **Grafana Dashboard**를 구축하여 '현재 대기 중인 신고 요청 수(RabbitMQ Depth)'와 'JVM 메모리 상태'를 실시간으로 모니터링합니다.
    * **Effect:** 장애 발생 시 로그를 뒤지는 대신, 대시보드를 통해 즉각적인 원인 파악 및 대응이 가능해졌습니다.

### 🏗️ 3. 배포 안정성: "기도 메타는 그만" (Flyway)

* **Problem:** 시스템 고도화 과정에서 DB 스키마 불일치로 인한 배포 장애 발생 가능성.
* **Solution:**
    * **Flyway**를 도입하여 모든 DB 스키마 변경(DDL)을 SQL 스크립트로 버전 관리합니다.
    * JPA의 `ddl-auto`에 의존하지 않고, 마이그레이션 이력을 추적하여 배포 실패 리스크를 최소화했습니다.

### 💰 4. 금융 정합성: "1원의 오차도 허용하지 않음" (Economic Logic)

* **Problem:** 부동소수점 오차 및 복잡한 누진세율 계산 로직의 유지보수 어려움.
* **Solution:**
    * 모든 금전 데이터에 `BigDecimal`을 적용하여 연산 정밀도를 보장했습니다.
    * 매년 개정되는 세법을 유연하게 반영하기 위해 전략 패턴(Strategy Pattern)을 사용하여 `TaxPolicy2024`, `TaxPolicy2025` 등으로 로직을 격리했습니다.

-----

## 4\. Tech Stack (기술 스택)

* **Language:** Java 17
* **Framework:** Spring Boot 3.x, Spring Data JPA
* **Database:** H2 (Local), MySQL (Production ready), Flyway
* **Messaging:** RabbitMQ (with Dead Letter Queue)
* **Traffic Control:** Bucket4j, Spring Interceptor
* **Monitoring:** Spring Actuator, Prometheus, Grafana
* **Test:** JUnit5, Mockito

-----

## 5\. How to Run (실행 방법)

### Prerequisites

* Docker & Docker Compose (RabbitMQ, Prometheus, Grafana 실행용)

### Steps

1.  **인프라 실행 (RabbitMQ)**
    ```bash
    docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
    ```
2.  **애플리케이션 실행**
    ```bash
    ./gradlew bootRun
    ```
3.  **테스트 (API)**
    * 세금 신고 요청: `POST /api/tax/calculate`
    * 모니터링 대시보드 접속: `http://localhost:3000` (Grafana)

-----

## 📮 Contact

* **Email:** prettywang777@gmail.com
* **Blog:** https://velog.io/@hello22433/posts
* **Note:** 이 프로젝트는 널리소프트의 비전인 "모두가 최저세금으로"에 기여할 수 있는 기술적 역량을 증명하기 위해 제작되었습니다.
