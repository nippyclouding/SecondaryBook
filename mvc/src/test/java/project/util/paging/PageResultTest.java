package project.util.paging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PageResult 페이지네이션 컨테이너 테스트
 * - 생성자로 전달된 값을 Getter가 그대로 반환하는지 검증
 * - 엣지 케이스: 빈 리스트, 전체 0건, 첫 페이지
 */
@DisplayName("PageResult - 페이지네이션 컨테이너")
class PageResultTest {

    @Test
    @DisplayName("생성자로 전달된 list, total, curPage, size를 그대로 반환한다")
    void constructor_storesAllValues() {
        List<String> list = Arrays.asList("a", "b", "c");
        PageResult<String> result = new PageResult<>(list, 100, 2, 10);

        assertThat(result.getList()).isEqualTo(list);
        assertThat(result.getTotal()).isEqualTo(100);
        assertThat(result.getCurPage()).isEqualTo(2);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("빈 리스트여도 정상 생성된다")
    void emptyList_ok() {
        PageResult<Object> result = new PageResult<>(Collections.emptyList(), 0, 1, 10);

        assertThat(result.getList()).isEmpty();
        assertThat(result.getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("total이 0이어도 정상 생성된다")
    void zeroTotal_ok() {
        PageResult<String> result = new PageResult<>(Collections.emptyList(), 0, 1, 20);
        assertThat(result.getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("첫 페이지(1) 정보를 정확히 보관한다")
    void firstPage_storedCorrectly() {
        List<Integer> data = Arrays.asList(1, 2, 3, 4, 5);
        PageResult<Integer> result = new PageResult<>(data, 5, 1, 5);

        assertThat(result.getCurPage()).isEqualTo(1);
        assertThat(result.getList()).hasSize(5);
    }
}
