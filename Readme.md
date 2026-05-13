# SecondHandBooks

**중고책 거래 및 독서 모임 플랫폼**

사용자는 중고책 판매글을 등록할 수 있으며,<br>
구매자는 판매자와 1:1 채팅 후 Toss Payments 기반 안전결제를 진행할 수 있습니다.<br>
거래 완료 후 구매확정 및 판매자 정산 요청 기능을 제공하며, 독서모임 커뮤니티 기능도 함께 운영됩니다..

---

<img width="643" height="574" alt="대표이미지" src="https://github.com/user-attachments/assets/f112c711-ca01-41c4-867a-b29661902be4" />

---

### 개발 과정 & 사용 기술

**개발 기간**
- 2개월 (2026.01.~2026.02.)

**개발 인원**
- 6명 (프론트엔드 2명, 백엔드 4명)

**개발 환경**
- **Language & DB**: Java 17, MySQL 8, Redis 8
- **Framework**: Spring Framework with MyBatis
- **Auth**: Session & Cookie
- **Real-Time**: STOMP WebSocket
- **Frontend**: Javascript, JSP (Server Side Rendering)
- **Infrastructure**: AWS EC2 & ELB with Auto Scaling & RDS, ElastiCache Redis, S3, CloudFront & CloudWatch


---

### 1. ERD

<img width="1618" height="999" alt="ERD" src="https://github.com/user-attachments/assets/cfb69d9e-1e9d-4ed0-8070-0e9c5af6a977" />

<p align="center">
  <a href="https://www.erdcloud.com/d/n5g78GXYfmokFXqiN">ERDCloud에서 자세히 보기</a>
</p>

---

### 2. SYSTEM ARCHITECTURE

<img width="1027" height="918" alt="SystemArchitecture" src="https://github.com/user-attachments/assets/5ae960d9-f287-4ad9-9db6-831555ff84fd" />


---

### 3. 주요 기능

#### **가방 종류 선택**
사용자는 로그인 후 고민을 담을 가방을 총 3가지 중 하나 선택할 수 있습니다. 

<img width="1870" height="949" alt="가방선택" src="https://github.com/user-attachments/assets/4edda114-fa59-47db-9572-96006528b653" />


#### **중고책 판매글 등록**
판매자는 중고책 판매글을 등록할 수 있으며,<br>
다중 이미지 업로드 및 카카오 도서 API 기반 도서 정보 조회를 통해 정확한 값을 등록할 수 있습니다.

<img width="735" height="848" alt="고민" src="https://github.com/user-attachments/assets/0170249d-7041-4130-80b5-568e3675a263" />

#### **거래 목록 조회**
사용자는 판매 상태, 카테고리, 검색어, 정렬 조건 기반으로 거래 목록을 조회할 수 있습니다.<br>
거래 목록은 페이징 처리되며 Redis Cache를 적용해 반복 조회 성능을 개선했습니다.

<img width="977" height="820" alt="응답" src="https://github.com/user-attachments/assets/a912b9c6-f81f-4cb8-b6af-400c80b69b78" />


#### **고민 게시글 전체 조회**
사용자는 전체 고민들을 조회할 수 있습니다. (페이징 처리)

<img width="765" height="613" alt="고민리스트" src="https://github.com/user-attachments/assets/64212bad-79ef-4e23-ac7b-fd14f882b804" />


#### **1:1 실시간 채팅**
구매자는 판매자와 실시간 1:1 채팅을 진행할 수 있습니다.<br> STOMP 기반 WebSocket 채팅을 구현했으며, Redis Pub/Sub을 적용해 멀티 서버 환경에서도 메시지를 동기화했습니다.


<img width="1889" height="944" alt="상세조회와좋아요" src="https://github.com/user-attachments/assets/2f8598ba-3ed1-42ee-af32-8565499912c9" />

#### **안전결제**
구매자는 Toss Payments 기반 안전결제를 진행할 수 있습니다.<br>
안전결제 상태를 NONE → PENDING → COMPLETED 흐름으로 관리했으며, 조건부 UPDATE를 적용해 중복 결제 및 비인가 결제 요청을 방지했습니다.

<img width="1889" height="944" alt="마이페이지" src="https://github.com/user-attachments/assets/42a7c383-7269-47bd-af0d-339d63938bb2" />


#### **구매확정 및 판매자 정산 요청**

안전 결제 이후 구매자는 배송 수령을 한 뒤 구매확정을 진행할 수 있으며,<br>
판매자는 구매확정된 거래에 대해 정산 요청을 할 수 있습니다. <br>
안전 결제 이후 15일 내에 구매확정이 없을 경우 스케줄러를 통해 자동으로 구매확정 처리 <br>
정산 상태는  NONE -> READY -> REQUESTED -> COMPLETED 입니다.

<img width="1895" height="985" alt="관리자" src="https://github.com/user-attachments/assets/34fc5568-d5fa-465b-9fd8-9c46d27a4736" />


#### **관리자 페이지**
관리자는 회원 관리, 안전 결제 내역 조회, 정산 신청 관리 등의 기능을 가집니다. <br>


---

### 4. 시연

SecondHandBooks의 주요 화면 흐름과 기능 동작을 영상으로 확인할 수 있습니다.

<a href="https://youtu.be/wkFMnX2pCsY">
  <img src="https://img.shields.io/badge/YouTube-시연%20영상%20보러가기-FF0000?style=for-the-badge&logo=youtube&logoColor=white" alt="시연 영상 보러가기" />
</a>
