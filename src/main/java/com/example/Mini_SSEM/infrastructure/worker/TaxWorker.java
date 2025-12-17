package com.example.Mini_SSEM.infrastructure.worker;

import com.example.Mini_SSEM.domain.model.TaxRecord;
import com.example.Mini_SSEM.domain.repository.TaxRecordRepository;
import com.example.Mini_SSEM.domain.service.TaxPolicy;
import com.example.Mini_SSEM.domain.service.TaxPolicy2024;
import com.example.Mini_SSEM.domain.service.TaxPolicyResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxWorker {

    //    private final TaxCalculator calculator;
//    private final TaxPolicy2024 calculator;
    private final TaxPolicyResolver policyResolver;
    private final TaxRecordRepository repository;


    // RabbitMQ 큐("tax-queue")를 바라보고 있다가 메시지가 오면 실행
    @Transactional
    @RabbitListener(queues = "tax-queue")
    public void processTaxCalculation(String requestId) {
        log.info("작업 시작: {}", requestId);

        // 1. DB에서 요청 정보 가져오기
        TaxRecord record = repository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("DB에 데이터가 없습니다!"));

        try {
            // 연도 기준으로 정책 선택 (OCP 원칙 위함. 연도가 변경되어도 코드를 바꾸지 않아도 됨)
            TaxPolicy policy = policyResolver.resolve(record.getYear());

            // 2. 핵심 비즈니스 로직 수행 (세금 계산)
            // Cargo 경험: 여기서 시간이 오래 걸려도 앞단 API 서버는 멈추지 않음
            BigDecimal tax = policy.calculate(record.getIncome());

            // 3. 결과 업데이트 (COMPLETED)
            record.complete(tax);
            log.info("계산 완료: 소득={}, 세금={}", record.getIncome(), tax);

        } catch (Exception e) {
            log.error("계산 실패", e);
            // 실무 팁: 여기서 실패하면 DLQ로 보내야 함
        }
    }

}
