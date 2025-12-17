package com.example.Mini_SSEM;

import com.example.Mini_SSEM.domain.service.TaxPolicy;
import com.example.Mini_SSEM.domain.service.TaxPolicy2024;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TaxPolicyTest {

    private final TaxPolicy taxPolicy = new TaxPolicy2024();

    @Test
    @DisplayName("과세표준 1400만원: 6% 세율 적용 검증")
    void calculate_section1() {
        // given
        BigDecimal income = new BigDecimal("10000000"); // 1,000만원

        // when
        BigDecimal tax = taxPolicy.calculate(income);

        // then (1000만원 * 0.06 = 60만원)
        assertThat(tax).isEqualByComparingTo(new BigDecimal("600000"));
    }

    @Test
    @DisplayName("과세표준 5000만원: 누진공제액 적용 검증(경계값)")
    void calculate_section2() {
        // given
        BigDecimal income = new BigDecimal("50000000"); // 5,000만원

        // when
        BigDecimal tax = taxPolicy.calculate(income);

        // then
        // 계산식: 50,000,000 * 0.15 - 1,260,000 = 6,240,000원
        assertThat(tax).isEqualByComparingTo(new BigDecimal("6240000"));
    }
}
