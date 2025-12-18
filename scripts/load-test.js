import http from 'k6/http';
import { check, sleep } from 'k6';

// 1. 테스트 설정 (가상 유저 100명이 10초 동안 공격)
export const options = {
    vus: 100, // Virtual Users (가상 유저 수)
    duration: '10s', // 테스트 지속 시간
};

export default function() {
    // 2. 공격할 타겟 및 데이터
    // 주의: 도커 내부에서 실행할 경우 localhost 대신 host.docker.internal 사용
    const url = 'http://host.docker.internal:8080/api/tax/calculate';
    const payload = JSON.stringify({
        year:2024,
        income: 50000000,
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // 3. POST 요청 발사 !
    const res = http.post(url, payload, params);

    // 4. 결과 검증 (Check)
    // 200이면 "성공", 429면 "방어 성공", 500이면 "서버 폭파(실패)"
    check(res, {
        '✅ 접수 성공 (200)': (r) => r.status === 200,
        '🛡️ 방어 성공 (429)': (r) => r.status === 429,
        '❌ 서버 에러 (500)': (r) => r.status === 500,
    });

    // 0.1초 휴식 후 다시 공격
    sleep(0.1);
}