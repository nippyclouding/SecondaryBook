package project.batch;

import org.springframework.batch.item.support.ListItemReader;
import project.settlement.SettlementMapper;
import project.settlement.SettlementVO;

/**
 * REQUESTED 상태인 정산 건 전체를 한 번에 로드하는 ItemReader.
 * Step 시작 시점에 DB에서 목록을 조회하고, 이후 read() 호출마다 1건씩 반환한다.
 * chunk = 1 이므로 1건씩 독립 트랜잭션으로 처리된다.
 */
public class SettlementItemReader extends ListItemReader<SettlementVO> {

    public SettlementItemReader(SettlementMapper settlementMapper) {
        super(settlementMapper.findAllRequested());
    }
}
