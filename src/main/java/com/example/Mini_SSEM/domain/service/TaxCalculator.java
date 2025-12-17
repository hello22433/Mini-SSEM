//package com.example.Mini_SSEM.domain.service;
//
//import org.springframework.stereotype.Component;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//
//@Component
//public class TaxCalculator {
//
//    // 2024년 귀속 종합소득세율 (간소화 버전)
//    // 과세표준 구간별 세율과 누진공제액을 정의
//    // 전략 패턴으로 분리 가능하지만, 직관성을 위해 우선 메서드로 구현
//    public BigDecimal calculate(BigDecimal income) {
//        // 방어 로직: 음수 소득은 0으로 처리
//        if (income.compareTo(BigDecimal.ZERO) <= 0) {
//            return BigDecimal.ZERO;
//        }
//
//        BigDecimal tax;
//
//        // 1. 1,400만원 이하 (세율 6%)
//        if (income.compareTo(new BigDecimal("14000000")) <= 0) {
//            tax = income.multiply(new BigDecimal("0.06"));
//        }
//        // 2. 5,000만원 이하 (세율 15%, 누진공제 126만원)
//        else if (income.compareTo(new BigDecimal("50000000")) <= 0) {
//            tax = income.multiply(new BigDecimal("0.15"))
//                    .subtract(new BigDecimal("1260000"));
//        }
//        // 3. 8,800만원 이하 (세율 24%, 누진공제 576만원)
//        else if (income.compareTo(new BigDecimal("88000000")) <= 0) {
//            tax = income.multiply(new BigDecimal("0.24"))
//                    .subtract(new BigDecimal("5760000"));
//        }
//        // ... 상위 구간 생략 (실제 구현 시 추가)
//        else {
//            // 예시: 8,800만원 초과 시 35% (누진공제 1,544만원)
//            tax = income.multiply(new BigDecimal("0.35"))
//                    .subtract(new BigDecimal("15440000"));
//        }
//
//        // 10원 단위 절사 (금융권 룰)
//        return tax.setScale(-1, RoundingMode.DOWN);
//    }
//}
