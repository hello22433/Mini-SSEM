-- outbox 테이블 생성

CREATE TABLE tax_outbox (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_id VARCHAR(36) COMMENT 'TaxRequest ID',
    event_type VARCHAR(50) COMMENT 'tax.calculate 등',
    payload TEXT COMMENT 'JSON 직렬화한 요청 데이터',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING, SENT 등',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Poller가 WHERE status = 'PENDING'로 계속 긁음
-- aggregate 기준 재처리/추적 가능
CREATE INDEX idx_tax_outbox_status ON tax_outbox (status);
CREATE INDEX idx_tax_outbox_aggregate ON tax_outbox (aggregate_id);

-- event 중복 방지
-- 트랜잭션 재시도, 서버 재기동, 네트워크 오류 시 이벤트 중복 발행 위험 방지
ALTER TABLE tax_outbox
ADD CONSTRAINT uk_outbox_event
UNIQUE (aggregate_id, event_type);

