package project.bookclub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import project.bookclub.vo.BookClubVO;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookClubPageResponseDTO {
    private List<BookClubVO> content;    // 모임 목록
    private int page;                     // 현재 페이지 (0부터 시작)
    private int size;                     // 페이지 크기
    private long totalElements;           // 전체 모임 수
    private int totalPages;               // 전체 페이지 수
    private boolean first;                // 첫 페이지 여부
    private boolean last;                 // 마지막 페이지 여부
}
