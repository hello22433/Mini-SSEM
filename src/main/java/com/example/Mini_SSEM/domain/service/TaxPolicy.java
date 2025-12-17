package com.example.Mini_SSEM.domain.service;

import java.math.BigDecimal;

// 세법은 변한다 -> 전략패턴 적용
// 단순 if-else 대신, 인터페이스를 사용하여 확장성 보장
public interface TaxPolicy {
    BigDecimal calculate(BigDecimal income); // 세금 계산 (무조건 BigDecimal)
    boolean supports(int year); // 해당 정챍이 지원하는 연도인지 확인
}
