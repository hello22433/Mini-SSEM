package com.example.Mini_SSEM.domain.service;

import com.example.Mini_SSEM.domain.model.OutboxEvent;
import com.example.Mini_SSEM.domain.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;

    // 실패하면 자동 재시도, 서버 재시작해도 이어서 처리, MQ 장애에도 안전
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publish() {
        List<OutboxEvent> events =
                outboxRepository.findByStatus("PENDING");

        for (OutboxEvent event : events) {
            try {
                rabbitTemplate.convertAndSend(
                        "tax-exchange",
                        event.getEventType(),
                        event.getAggregateId()
                );
                event.markSent();
            } catch (Exception e) {
                // 실패해도 상태 유지 -> 다음 턴에 재시도
            }
        }
    }
}
