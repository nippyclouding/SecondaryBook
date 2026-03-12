import http from 'k6/http';
  import { check, sleep } from 'k6';

  export const options = {
      stages: [
          { duration: '1m', target: 100 },
          { duration: '2m', target: 100 },
          { duration: '1m', target: 200 },
          { duration: '2m', target: 200 },
          { duration: '1m', target: 300 },
          { duration: '2m', target: 300 },
          { duration: '1m', target: 400 },
          { duration: '2m', target: 400 },
          { duration: '2m', target: 0 },
      ],
      thresholds: {
          http_req_duration: ['p(95)<3000'],
          http_req_failed: ['rate<0.10'],
      },
  };

  const BASE_URL = 'https://www.shinhan6th.com';

  export default function () {
      let res1 = http.get(`${BASE_URL}/`);
      check(res1, { '메인 200': (r) => r.status === 200 });
      sleep(0.5);

      let res2 = http.get(`${BASE_URL}/trade/4`);
      check(res2, { '상품목록 200': (r) => r.status === 200 });
      sleep(0.5);

      let res3 = http.get(`${BASE_URL}/health`);
      check(res3, { '헬스 200': (r) => r.status === 200 });
      sleep(0.5);
  }