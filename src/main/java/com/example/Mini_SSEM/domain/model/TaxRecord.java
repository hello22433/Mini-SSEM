package com.example.Mini_SSEM.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor
public class TaxRecord {

    @Id
    private String id; // UUID

    private BigDecimal income; // 입력 소득
    private BigDecimal taxAmount; // 계산된 세금

    @Column(name = "tax_year", nullable = false)
    private int year; // 귀속 연도


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaxStatus status;

    public TaxRecord(String id, BigDecimal income, int year) {
        this.id = id;
        this.income = income;
        this.status = TaxStatus.PENDING;
        this.year = year; // 귀속 연도
    }

    // 세금 계산 완료
    public void complete(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
        this.status = TaxStatus.COMPLETED;
    }
}
