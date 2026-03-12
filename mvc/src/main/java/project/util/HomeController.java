package project.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import project.admin.AdminService;
import project.admin.BannerVO;
import project.admin.TempPageVO;
import project.chat.message.MessageService;
import project.trade.ENUM.SaleStatus;
import project.trade.TradeService;
import project.trade.TradeVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Handles requests for the application home page.
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class HomeController {

	private final TradeService tradeService;
	private final AdminService adminService;

	// 홈 화면 전체 판매글 출력
	@GetMapping({"/", "/home"})
	public String home(@RequestParam(defaultValue = "1") int page,
					   TradeVO searchVO, Model model,
					   HttpServletRequest request,
					   HttpServletResponse response) {
		int size = 14;  // 한 페이지에 14개

		// 초기 페이지 로드 시 sale_st 기본값 설정 (JS의 tradeFilter.sale_st='SALE'과 동일)
		// null이면 AJAX 재요청 시 캐시 키가 달라져 불필요한 캐시 중복 생성됨
		if (searchVO.getSale_st() == null) {
			searchVO.setSale_st(SaleStatus.SALE);
		}

		log.info("tradeVO 판매중, 완료, 전체 :" + searchVO.getSale_st());
		List<TradeVO> trades = tradeService.searchAllWithPaging(page, size, searchVO);
		int totalCount = tradeService.countAll(searchVO);
		int totalPages = (int) Math.ceil((double) totalCount / size);
		List<TradeVO> category = tradeService.selectCategory();	// 카테고리 조회
		List<BannerVO> banners = adminService.getBanners(); // 배너 조회


		model.addAttribute("totalCount", totalCount);
		model.addAttribute("trades", trades);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", totalPages);
		model.addAttribute("category", category);
		model.addAttribute("bannerList", banners); // 배너 조회



		// AJAX 요청이면 fragment만 반환
		if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
			response.setHeader("X-Total-Count", String.valueOf(totalCount));
			return "trade/tradelist";
		}
		return "common/home";
	}

	@GetMapping("/page/view/{id}")
	public String viewPage(@PathVariable Long id, Model model) {
		TempPageVO page = adminService.getTempPage(id);
		model.addAttribute("page", page);
		return "common/tempPage"; // 내용을 보여줄 jsp
	}



	@GetMapping("/presentation")
	public String intro() {
		return "presentation/intro";
	}

	@GetMapping("/presentation/api/stats")
	@ResponseBody
	public Map<String, Object> getProjectStats() {
		// 간단한 AJAX 데이터 제공 예시 (실제 DB 연동 가능)
		Map<String, Object> stats = new HashMap<>();
		stats.put("members", 1250);
		stats.put("books", 5430);
		stats.put("clubs", 82);
		return stats;
	}
}
