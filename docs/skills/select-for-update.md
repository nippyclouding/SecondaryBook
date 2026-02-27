# SELECT FOR UPDATE - 동시성 제어 전략

## 개념

`SELECT ... FOR UPDATE`는 조회한 행에 **배타적 잠금(exclusive lock)**을 거는 SQL 구문이다.
해당 트랜잭션이 커밋/롤백될 때까지 다른 트랜잭션은 그 행을 수정하거나 잠금할 수 없다.

```sql
-- 관리자 잔액 조회 시 행 잠금
SELECT balance FROM sb_account WHERE account_seq = 1 FOR UPDATE;

-- 이 시점부터 다른 트랜잭션은 이 행에 접근 대기
UPDATE sb_account SET balance = balance - 12870 WHERE account_seq = 1;
COMMIT; -- 잠금 해제
```

"읽기 → 계산 → 쓰기" 패턴에서 읽기와 쓰기 사이에 다른 트랜잭션이 값을 변경하는 것을 방지한다.

## 적용 위치

| 위치 | 용도 |
|------|------|
| `SettlementMapper.getAdminBalance()` | 정산 배치 처리 시 관리자 잔액 조회 + 차감의 원자성 보장 |
| `TradeMapper.findSafePaymentStatus()` | 안전결제 상태 조회 + 변경의 원자성 보장 |

## 대안 비교 및 선택 이유

| 방식 | 잠금 범위 | 성능 | 비고 |
|------|----------|------|------|
| **SELECT FOR UPDATE** | 해당 행만 | 좋음 | 필요한 곳에만 정밀하게 잠금 |
| SERIALIZABLE 격리 수준 | 트랜잭션 내 모든 쿼리 | 나쁨 | 불필요한 쿼리까지 전부 잠금 |
| REPEATABLE READ 격리 수준 | 읽은 행 전체 | 보통 | lost update 방지 가능하나 범위가 넓음 |
| 낙관적 락 (version 컬럼) | 잠금 없음 | 좋음 | 충돌 시 재시도 로직 필요, 스키마 변경 필요 |

**SELECT FOR UPDATE를 선택한 이유:**

- 트랜잭션 격리 수준을 올리면 해당 트랜잭션의 **모든 SELECT**가 영향을 받는다. 정산 처리 시 settlement 조회, trade 조회 등 잠금이 불필요한 쿼리까지 성능 저하가 발생한다.
- SELECT FOR UPDATE는 `sb_account`의 1행만 잠근다. 나머지 쿼리는 자유롭게 동작한다.
- 낙관적 락은 충돌 빈도가 낮을 때 유리하지만, 잔액 차감처럼 **충돌 시 데이터 정합성이 치명적인 경우** 비관적 락이 안전하다.

즉, 칼이 필요한 곳에 칼만 쓴 것이고, 격리 수준 향상은 모든 곳에 방탄유리를 까는 것이다.
