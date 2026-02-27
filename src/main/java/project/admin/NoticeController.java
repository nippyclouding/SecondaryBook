package project.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import project.util.paging.PageResult;
import project.util.paging.SearchVO;

import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class NoticeController {

    private final AdminService adminService;


    @GetMapping("/notice")
    public String userNoticeList(SearchVO searchVO, Model model) {

        searchVO.setStatus("true");

        List<NoticeVO> list = adminService.selectActiveNotices(searchVO);

        // 2. 전체 개수를 가져옵니다. (페이징 계산용 메서드가 필요함)
        int totalCount = adminService.countActiveNotices(searchVO);

        // 3. PageResult 바구니에 담습니다.
        PageResult<NoticeVO> result = new PageResult<>(list, totalCount, searchVO.getPage(), searchVO.getSize());

        model.addAttribute("result", result);
        return "userNotice/userNoticeList";
    }

    // 2. 유저용 공지사항 상세 보기 페이지로 이동
    @GetMapping("/notice/view")
    public String userNoticeView(@RequestParam Long notice_seq, Model model) {
        // 조회수 증가
        adminService.increaseViewCount(notice_seq);

        // 데이터 조회
        NoticeVO noticeVO = adminService.selectNotice(notice_seq);
        model.addAttribute("notice", noticeVO);

        return "userNotice/noticeDetail";
    }
}
