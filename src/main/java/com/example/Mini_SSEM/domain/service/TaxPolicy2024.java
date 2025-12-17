package com.example.Mini_SSEM.domain.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

// 내년 세법이 바뀌면 TaxPolicy2025클래스만 추가하여 기존 코드는 건드리지 아니함
// OCP 원칙
@Component
public class TaxPolicy2024 implements TaxPolicy {

    // 금융 계산용 상수 (매번 new하지 않게 정의)
    private static final BigDecimal SECTION_1 = new BigDecimal("14000000"); // 1400만원
    private static final BigDecimal SECTION_2 = new BigDecimal("50000000"); // 5000만원
    private static final BigDecimal TAX_RATE_1 = new BigDecimal("0.06"); // 6%
    private static final BigDecimal TAX_RATE_2 = new BigDecimal("0.15"); // 15%
    private static final BigDecimal DEDUCTION_2 = new BigDecimal("1260000"); // 누진공제액

    @Override
    public boolean supports(int year) {
        return year == 2024;
    }

    @Override
    public BigDecimal calculate(BigDecimal income) {
        // 방어 로직 : 음수 소득은 0원 처리
        if (income.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal tax;

        // 1. 과세표준 1,400만원 이하 (세율 6%)
        if (income.compareTo(SECTION_1) <= 0) {
            tax = income.multiply(TAX_RATE_1);
        }
        // 2. 과세표준 5,000만원 이하 (세율 15% - 누진공제 126만원)
        else if (income.compareTo(SECTION_2) <= 0) {
            tax = income.multiply(TAX_RATE_2).subtract(DEDUCTION_2);
        }
        // 상위 구간 생략
        else {
            tax = income.multiply(new BigDecimal("0.24").subtract(new BigDecimal("5760000")));
        }

        // 10원 단위 절사
        return tax.setScale(-1, RoundingMode.DOWN);

    }

}
