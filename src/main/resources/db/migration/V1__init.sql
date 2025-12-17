-- 세금 신고 기록 테이블
-- Entity: TaxRecord와 1:1 매칭

CREATE TABLE tax_record
(
    id         VARCHAR(36) NOT NULL COMMENT '요청 UUID',
    income     DECIMAL(19, 2) COMMENT '신고 소득 (BigDecimal)',
    tax_amount DECIMAL(19, 2) COMMENT '계산된 세금 (계산 전엔 NULL)',
    tax_year   INT          NOT NULL COMMENT '세금 정책 년도(정책 적용을 위한 값)',
    status     VARCHAR(20) NOT NULL COMMENT '상태 (PENDING, PROCESSING, COMPLETED, FAILED)',
    CONSTRAINT pk_tax_record PRIMARY KEY (id)
);

-- (선택사항) 검색 성능을 위한 인덱스 추가
-- 상태별로 조회를 많이 할 테니 인덱스 걸기
CREATE INDEX idx_tax_record_status ON tax_record (status);