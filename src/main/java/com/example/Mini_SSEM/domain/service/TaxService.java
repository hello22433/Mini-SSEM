package com.example.Mini_SSEM.domain.service;

import com.example.Mini_SSEM.domain.model.OutboxEvent;
import com.example.Mini_SSEM.domain.model.TaxRecord;
import com.example.Mini_SSEM.domain.model.TaxRequest;
import com.example.Mini_SSEM.domain.repository.OutboxRepository;
import com.example.Mini_SSEM.domain.repository.TaxRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxService {

    private final TaxRecordRepository taxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final OutboxRepository outBoxRepository;

    // TODO: 아웃박스 패턴 적용 필요
    @Transactional // 아웃박스에 데이터 저장하는 것을 하나의 트랜잭션으로 묶음
    public String submitTaxCalculation(TaxRequest taxRequest) {
        // 1. 요청 ID 생성
        String requestId = UUID.randomUUID().toString();

        // 2. DB에 PENDING 상태로 저장
        taxRepository.save(new TaxRecord(requestId, taxRequest.getIncome(), taxRequest.getYear()));

        // 3. 아웃박스 테이블에 이벤트 저장
        // (아웃박스로 리팩토링) 3. RabbitMQ 큐로 전송
//        rabbitTemplate.convertAndSend("tax-exchange", "tax.calculate", requestId);
        OutboxEvent event = new OutboxEvent(
                requestId,
                "tax.calculate",
                objectMapper.writeValueAsString(taxRequest)
        );
        outBoxRepository.save(event);

        return requestId;
    }


    /**
     * 세금 계산 결과 조회
     * @Cacheable 동작 원리:
     * 1. Redis에 key("tax_record::requestId")가 있는지 확인.
     * 2. 있으면 DB 안 가고 바로 리턴 (Cache Hit).
     * 3. 없으면 메서드 실행(DB 조회) 후 리턴값을 Redis에 저장 (Cache Miss).
     * unless 조건: 상태가 COMPLETED가 아니면 캐싱하지 않음 (PENDING은 자주 변하므로)
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "tax_record", key = "#requestId", unless = "#result.status.name() != 'COMPLETED'")
    public TaxRecord getTaxRecord(String requestId) {
        log.info("Cache Miss! DB에서 조회합니다: {}", requestId);
        return taxRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 신고내역입니다."));
    }

}
