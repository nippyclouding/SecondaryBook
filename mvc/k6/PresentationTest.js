import http from 'k6/http';
 import { check, sleep } from 'k6';

 export const options = {
     stages: [
         // 30명이 30분 동안 방문하는 시나리오
         { duration: '2m', target: 30 },   // 2분 동안 30명까지 증가
         { duration: '26m', target: 30 },  // 26분 동안 30명 유지
         { duration: '2m', target: 0 },    // 2분 동안 종료
     ],
     thresholds: {
         http_req_duration: ['p(95)<1000'],  // 95% 요청 1초 이내
         http_req_failed: ['rate<0.05'],     // 에러율 5% 미만
     },
 };

 // 테스트 대상 서버
 const BASE_URL = 'https://www.shinhan6th.com';

 // DB에 실제 존재하는 상품 ID
  const productIds = [40, 41];

  export default function () {
      // 메인 페이지 접속
      let res1 = http.get(`${BASE_URL}/`);
      check(res1, { '메인 200': (r) => r.status === 200 });
      sleep(Math.random() * 3 + 2); // 2~5초 랜덤 대기

      // 상품 상세 페이지 (랜덤 상품)
      let productId = productIds[Math.floor(Math.random() * productIds.length)];
      let res2 = http.get(`${BASE_URL}/trade/${productId}`);
      check(res2, { '상품상세 200': (r) => r.status === 200 });
      sleep(Math.random() * 5 + 3); // 3~8초 랜덤 대기

      // 헬스체크
      let res3 = http.get(`${BASE_URL}/health`);
      check(res3, { '헬스 200': (r) => r.status === 200 });
      sleep(Math.random() * 2 + 1); // 1~3초 랜덤 대기
  }