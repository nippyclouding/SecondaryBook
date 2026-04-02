package project.util.paging;

import lombok.Getter;

import java.util.List;

@Getter
public class PageResult<T> {
    private List<T> list;
    private int total; // 전체 데이터 개수
    private int curPage; // 현재 페이지 번호
    private int size; // 한 페이지당 보여줄 개수

    public PageResult(List<T> list, int total, int curPage, int size) {
        this.list = list;
        this.total = total;
        this.curPage = curPage;
        this.size = size;
    }
}
