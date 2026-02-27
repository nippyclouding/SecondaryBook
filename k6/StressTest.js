

  import http from 'k6/http';
  import { check, sleep } from 'k6';

  export const options = {
      stages: [
          // 1단계: 30명 (워밍업)
          { duration: '1m', target: 30 },
          { duration: '3m', target: 30 },

          // 2단계: 100명
          { duration: '1m', target: 100 },
          { duration: '5m', target: 100 },

          // 3단계: 200명
          { duration: '1m', target: 200 },
          { duration: '5m', target: 200 },

          // 4단계: 350명
          { duration: '1m', target: 350 },
          { duration: '5m', target: 350 },

          // 5단계: 500명 (최대)
          { duration: '1m', target: 500 },
          { duration: '5m', target: 500 },

          // 종료
          { duration: '2m', target: 0 },
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