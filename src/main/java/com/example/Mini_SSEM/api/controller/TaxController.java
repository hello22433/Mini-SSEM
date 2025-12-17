package com.example.Mini_SSEM.api.controller;

import com.example.Mini_SSEM.domain.model.TaxRecord;
import com.example.Mini_SSEM.domain.model.TaxRequest;
import com.example.Mini_SSEM.domain.model.TaxResponse;
import com.example.Mini_SSEM.domain.repository.TaxRecordRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tax")
public class TaxController {

    private final RabbitTemplate rabbitTemplate;
    private final TaxRecordRepository repository;

    // 문지기 설정 (Rate Limiter)
    // 용량(Capacity): 10개 (한 번에 최대로 처리 가능한 버스트 용량)
    // 충전(Refill): 1초에 10개씩 토큰 충전
    private final Bucket bucket = Bucket.builder()
            .addLimit(Bandwidth.classic(10, Refill.greedy(10, Duration.ofSeconds(1))))
            .build();

    // 1. 세금 신고 요청 (Non-Blocking) - 문지기 적용
    @PostMapping("/calculate")
    public ResponseEntity<TaxResponse> requestCalculation(@RequestBody TaxRequest request) {

        // Interceptor 로 설정 변경
        // TODO : 사용자별, IP별 제어 필요, Redis 기반 Bucket, Filter/Gateway 이전 이동 필요
//        // 🛑 입장권 검사: 토큰 1개 소모 시도
//        if (bucket.tryConsume(1)) {
            // [성공] 입장권 있음 -> 정상 처리
            String requestId = UUID.randomUUID().toString();
            log.info("요청 접수 성공: {}", requestId);

            // A. DB에 '접수(PENDING)' 상태로 우선 저장 (이력 남기기)
            repository.save(new TaxRecord(requestId, request.getIncome(), request.getYear()));

            // B. RabbitMQ 큐로 메시지 전송 (비동기 처리)
            // Exchange 이름: "tax-exchange", RoutingKey: "tax.calculate"
            rabbitTemplate.convertAndSend("tax-exchange", "tax.calculate", requestId);

            // C. 사용자에겐 "접수되었습니다" 라고 즉시 응답
            return ResponseEntity.ok(
                    new TaxResponse(
                            requestId,
                            "PENDING",
                            " 예상 세액 계산이 접수되었습니다. 잠시 후 조회해주세요."
                    )
            );

//        } else {
//            // [실패] 입장권 없음 -> 429 에러 리턴 (서버 보호)
//            log.warn("접속자 폭주! 요청 거절됨.");
//            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
//                    .body(new TaxResponse(
//                    null,
//                            "FAILED",
//                            "현재 접속자가 너무 많아 대기 중입니다. 1초 뒤 다시 시도해주세요.")
//                            );
//
//        }


    }

    // 2. 결과 조회 (Polling용)
    @GetMapping("{requestId}")
    public TaxRecord getResult(@PathVariable String requestId) {
        return repository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("없는 요청입니다."));
    }
}
