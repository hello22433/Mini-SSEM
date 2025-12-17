package com.example.Mini_SSEM.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaxPolicyResolver {

    private final List<TaxPolicy> policies;

    public TaxPolicy resolve(int year) {
        return policies.stream()
                .filter(p -> p.supports(year))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 연도"));
    }
}
