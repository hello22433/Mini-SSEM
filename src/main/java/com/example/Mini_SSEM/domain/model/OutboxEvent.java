package com.example.Mini_SSEM.domain.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "tax_outbox")
public class OutboxEvent {

    // 비즈니스 로직 없음
    // 상태 전이만 책임

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String aggregateId;
    private String eventType;

    @Lob
    private String payload;

    private String status = "PENDING";

    protected OutboxEvent() {}

    public OutboxEvent(String aggregateId, String eventType, String payload) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public void markSent() {
        this.status = "SENT";
    }
}
