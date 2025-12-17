package com.example.Mini_SSEM.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaxResponse {
    private String requestId;
    private String status;
    private String message;
}
