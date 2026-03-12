  import http from 'k6/http';
  import { check, sleep } from 'k6';

  export const options = {
      stages: [
          { duration: '30s', target: 200 },   // 워밍업
          { duration: '1m', target: 500 },    // 스케일 시작
          { duration: '1m', target: 800 },    // 스케일 진행
          { duration: '1m', target: 1200 },   // 고부하
          { duration: '2m', target: 1200 },   // 고부하 유지
          { duration: '1m', target: 1500 },   // 최대 부하
          { duration: '2m', target: 1500 },   // 최대 유지
          { duration: '1m', target: 0 },      // 종료
      ],
      thresholds: {
          http_req_duration: ['p(95)<5000'],
          http_req_failed: ['rate<0.20'],
      },
  };

  const BASE_URL = 'https://www.shinhan6th.com';

  export default function () {
      let res1 = http.get(`${BASE_URL}/`);
      check(res1, { '메인 200': (r) => r.status === 200 });

      let res2 = http.get(`${BASE_URL}/trade/4`);
      check(res2, { '상품목록 200': (r) => r.status === 200 });

      let res3 = http.get(`${BASE_URL}/health`);
      check(res3, { '헬스 200': (r) => r.status === 200 });

      sleep(0.3);  // 더 빠르게 요청
  }
