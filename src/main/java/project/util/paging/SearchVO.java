package project.util.paging;

import lombok.Data;


@Data
public class SearchVO {
    private int page = 1; // 기본값 1페이지
    private int size = 10; // 기본값 10개씩
    private String keyword; // 검색어
    private String searchType; // 검색타입 (all, nicknm)
    private String status; // 상태필터

    public int getOffset() {
        return (this.page - 1) * this.size;
    }


}
