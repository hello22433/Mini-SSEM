package com.example.Mini_SSEM.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TaxRequest {
    private BigDecimal income;
    private int year;
}
