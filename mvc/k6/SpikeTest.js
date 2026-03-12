import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        // 안정 상태
        { duration: '2m', target: 10 },  // 초기 10명

        // 스파이크: 10명 → 500명
        { duration: '30s', target: 500 }, // 30초 동안 급증
        { duration: '2m', target: 500 },  // 2분 유지

        // 급격하게 줄이기
        { duration: '30s', target: 10 },  // 30초 동안 감소
        { duration: '2m', target: 10 },   // 안정 유지

        // 종료
        { duration: '1m', target: 0 },
    ],
    thresholds: {
        http_req_duration: ['p(95)<3000'],
        http_req_failed: ['rate<0.10'],
    },
};

const BASE_URL = 'https://www.shinhan6th.com';
const productIds = [40, 41];

export default function () {
    let res1 = http.get(`${BASE_URL}/`);
    check(res1, { '메인 200': (r) => r.status === 200 });
    sleep(0.5);

    let productId = productIds[Math.floor(Math.random() * productIds.length)];
    let res2 = http.get(`${BASE_URL}/trade/${productId}`);
    check(res2, { '상품상세 200': (r) => r.status === 200 });
    sleep(0.5);

    let res3 = http.get(`${BASE_URL}/health`);
    check(res3, { '헬스 200': (r) => r.status === 200 });
    sleep(0.5);
}
