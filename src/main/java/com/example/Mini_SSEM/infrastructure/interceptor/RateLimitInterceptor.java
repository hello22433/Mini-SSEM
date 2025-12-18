package com.example.Mini_SSEM.infrastructure.interceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Bucket bucket = Bucket.builder()
            .addLimit(Bandwidth.classic(
                    50, Refill.greedy(50, Duration.ofSeconds(1))
            ))
            .build();


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler
        ) throws Exception {

        if (bucket.tryConsume(1)) {
            return true;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write("""
                {
                    "status": "FAILED",
                    "message": "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."
                }
                """);
        return false;
    }
}
