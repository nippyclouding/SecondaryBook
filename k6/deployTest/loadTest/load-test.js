
  import http from 'k6/http';
  import { check, sleep } from 'k6';


  export const options = { // options = 부하 시나리오 설정
      stages: [
          //          30초 동안 → 10명까지 점점 증가
          //          1분 동안 → 50명 유지
          //          30초 동안 → 0명으로 감소 (테스트 종료)
          //          실제 사용자 유입/이탈 상황을 흉내내는 테스트
          { duration: '30s', target: 10 },   // 10명까지 증가
          { duration: '1m', target: 50 },    // 50명 유지
          { duration: '30s', target: 0 },    // 종료

      ],
      thresholds: { // 합격 기준
          //        95% 요청이 500ms 안에 끝나야 함
          //        에러율 1% 미만이어야 함
          //        하나라도 깨지면 k6가 실패로 판단
          http_req_duration: ['p(95)<500'],  // 95% 요청 500ms 이내
          http_req_failed: ['rate<0.01'],    // 에러율 1% 미만

      },
  };

  // 테스트 대상 서버
  const BASE_URL = 'https://www.shinhan6th.com';

  export default function () { // 실제 사용자 행동 시뮬레이션, k6가 이 함수를 동시에 여러 명(VU) 으로 실행
      // 메인 페이지 : trade list
      let res1 = http.get(`${BASE_URL}/`); // 1. localhost:8080/ 접속
      check(res1, { '메인 200': (r) => r.status === 200 }); // 2. 200 OK 확인
      sleep(1); // 3. 1초 쉬기

      // 상품 상세 페이지 (4번 상품)
      let res2 = http.get(`${BASE_URL}/trade/4`); // 1. localhost:8080/trade/4 접속
      check(res2, { '상품목록 200': (r) => r.status === 200 }); // 2. 200 OK 확인
      sleep(1); // 3. 1초 쉬기

      // 헬스체크
      let res3 = http.get(`${BASE_URL}/health`); // 1. localhost:8080/health 접속
      check(res3, { '헬스 200': (r) => r.status === 200 }); // 2. 200 OK 확인
      sleep(1); // 3. 1초 쉬기
  }