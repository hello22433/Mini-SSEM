package com.example.Mini_SSEM.domain.model;

public enum TaxStatus {
    PENDING, // 접수됨 (큐 대기 중)
    PROCESSING, // 계산 중
    COMPLETED, // 완료
    FAILED, // 실패
}
