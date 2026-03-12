package spring.batch.job.settlement;

/**
 * 관리자 잔액 부족 시 발생하는 예외.
 * Spring Batch skip 대상으로 설정되어 있어 해당 건은 건너뛰고 다음 건을 처리한다.
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
