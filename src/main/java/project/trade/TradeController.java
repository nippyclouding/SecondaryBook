package project.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import project.member.MemberVO;
import project.util.Const;
import project.util.imgUpload.ImgService;
import project.util.book.BookApiService;
import project.util.book.BookVO;
import project.util.exception.InvalidRequestException;
import project.util.exception.ServerException;
import project.util.imgUpload.FileStore;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class TradeController {
    private final TradeService tradeService;
    private final BookApiService bookApiService;
    private final FileStore fileStore; // 이미지 저장 기능을 수행하는 객체
    private final ImgService imgService; // @Primary → FileUploadService

    // 판매글 단일 조회
    @GetMapping("/trade/{tradeSeq}")
    public String getSaleDetail(@PathVariable long tradeSeq, Model model, HttpSession session) {
        TradeVO trade = tradeService.search(tradeSeq);

        int wishCount = tradeService.countLikeAll(tradeSeq); // 총 찜 개수
        boolean wished = false;

        MemberVO login = (MemberVO) session.getAttribute(Const.SESSION);
        MemberVO seller_info = tradeService.findSellerInfo(tradeSeq);   // 판매자 정보조회
        if (login != null) {
            wished = tradeService.isWished(tradeSeq, login.getMember_seq());    // 찜하기 눌렀는지 검증
        }
        model.addAttribute("seller_info", seller_info);
        model.addAttribute("trade", trade);
        model.addAttribute("wishCount", wishCount);
        model.addAttribute("wished", wished);

        return "trade/tradedetail";
    }

    // 판매글 등록 페이지
    @GetMapping("/trade")
    public String getTrade(Model model, HttpSession session) {
        // 인터셉터가 로그인 검증 완료
        // 카테고리 데이터 add
        model.addAttribute("category", tradeService.selectCategory());
        return "trade/tradeform";
    }


    // 판매글 create
    @PostMapping("/trade")
    public String uploadTrade(@Valid TradeVO tradeVO, BindingResult bindingResult,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) throws Exception {
        // 인터셉터가 로그인 검증 완료
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);

        if (bindingResult.hasErrors()) {
            log.warn("Trade validation error: {}", bindingResult.getAllErrors());
            throw new InvalidRequestException("입력 값이 올바르지 않습니다.");
        }

        tradeVO.setMember_seller_seq(sessionMember.getMember_seq());

        // 이미지 파일 처리 (서버에 uuid 이름으로 저장, db 에 실제 이름으로 저장)
        List<MultipartFile> uploadFiles = tradeVO.getUploadFiles(); // form 에서 받은 데이터 조회
        log.info("uploadFiles: {}", uploadFiles);

        if (uploadFiles != null && !uploadFiles.isEmpty()) {
            List<String> imgUrls = imgService.storeFiles(uploadFiles);  // S3에 업로드, URL 리스트 반환
            log.info("imgUrls: {}", imgUrls);
            tradeVO.setImgUrls(imgUrls);
        }

        if (tradeService.upload(tradeVO)) {
            log.info("trade save success, book isbn : {}", tradeVO.getIsbn());
            redirectAttributes.addAttribute("tradeSeq", tradeVO.getTrade_seq());
            return "redirect:/trade/{tradeSeq}";
        }
        // 실패 시 - S3에 업로드된 이미지 정리
        List<String> uploadedUrls = tradeVO.getImgUrls();
        if (uploadedUrls != null) {
            for (String url : uploadedUrls) {
                try { imgService.deleteByUrl(url); } catch (Exception ex) { log.error("S3 cleanup failed: {}", url, ex); }
            }
        }
        throw new ServerException("판매글 등록에 실패했습니다.");
    }

    // 판매글 update 요청
    @GetMapping("/trade/modify/{tradeSeq}")
    public String modifyRequest(@PathVariable Long tradeSeq, Model model, HttpSession session) {
        // 인터셉터가 로그인 검증 완료
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);

        // 권한 검증 (판매자 + 상태 체크)
        tradeService.validateCanModify(tradeSeq, sessionMember.getMember_seq());

        TradeVO trade = tradeService.search(tradeSeq);

        // 카테고리 데이터 add
        model.addAttribute("category", tradeService.selectCategory());
        model.addAttribute("trade", trade);
        return "trade/tradeupdate";
    }

    // 판매글 update 등록
    @PostMapping("/trade/modify/{tradeSeq}")
    public String modifyUpload(@PathVariable Long tradeSeq, @Valid TradeVO updateTrade, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, HttpSession session) throws Exception {
        // 인터셉터가 로그인 검증 완료
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);

        if (bindingResult.hasErrors()) {
            log.warn("Trade update validation error: {}", bindingResult.getAllErrors());
            throw new InvalidRequestException("입력 값이 올바르지 않습니다.");
        }

        // 권한 검증 (판매자 + 상태 체크)
        tradeService.validateCanModify(tradeSeq, sessionMember.getMember_seq());

        // updateTrade에 seller seq 할당
        updateTrade.setMember_seller_seq(sessionMember.getMember_seq());

        // 이미지 파일 처리
        List<MultipartFile> uploadFiles = updateTrade.getUploadFiles();
        List<String> keepImageUrls = updateTrade.getKeepImageUrls(); // 유지할 기존 이미지
        List<String> finalImgUrls = new ArrayList<>();
        List<String> newlyUploadedUrls = new ArrayList<>(); // DB 저장 실패 시 정리용

        // 1. 유지할 기존 이미지 추가
        if (keepImageUrls != null && !keepImageUrls.isEmpty()) {
            finalImgUrls.addAll(keepImageUrls);
            log.info("keepImageUrls: {}", keepImageUrls);
        }

        // 2. 새로 업로드된 이미지 추가
        if (uploadFiles != null && !uploadFiles.isEmpty()) {
            // 빈 파일 제외하고 실제 파일만 필터링
            List<MultipartFile> validFiles = new ArrayList<>();
            for (MultipartFile file : uploadFiles) {
                if (file != null && !file.isEmpty()) {
                    validFiles.add(file);
                }
            }
            if (!validFiles.isEmpty()) {
                newlyUploadedUrls = imgService.storeFiles(validFiles);  // S3에 업로드
                finalImgUrls.addAll(newlyUploadedUrls);
                log.info("newImgUrls: {}", newlyUploadedUrls);
            }
        }

        log.info("finalImgUrls: {}", finalImgUrls);
        updateTrade.setImgUrls(finalImgUrls.isEmpty() ? null : finalImgUrls);

        // 수정에 성공했을 때
        if (tradeService.modify(tradeSeq, updateTrade)) {
            log.info("update Success");
            redirectAttributes.addAttribute("tradeSeq", tradeSeq);
            return "redirect:/trade/{tradeSeq}";
        }

        // 실패 시 - 새로 업로드된 이미지만 정리 (keepImageUrls는 기존 파일이므로 제외)
        for (String url : newlyUploadedUrls) {
            try { imgService.deleteByUrl(url); } catch (Exception ex) { log.error("S3 cleanup failed: {}", url, ex); }
        }
        throw new ServerException("판매글 수정에 실패했습니다.");
    }

    // 판매글 delete
    @PostMapping("/trade/delete/{tradeSeq}")
    public String remove(@PathVariable Long tradeSeq,
                         RedirectAttributes redirectAttributes, HttpSession session) throws Exception {
        // 인터셉터가 로그인 검증 완료
        MemberVO sessionMember = (MemberVO) session.getAttribute(Const.SESSION);

        // 권한 검증 (판매자 + 상태 체크)
        tradeService.validateCanDelete(tradeSeq, sessionMember.getMember_seq());

        if (tradeService.remove(tradeSeq)) {
            log.info("delete Success");
            return "redirect:/";
        }
        throw new ServerException("판매글 삭제에 실패했습니다.");
    }


    // 도서 검색
    @GetMapping("/trade/book")
    @ResponseBody
    public List<BookVO> findBookByTitle(@RequestParam String query) { // query = 검색어
        // query로 책 검색
        log.info(query);
        return bookApiService.searchBooks(query);
    }

    // 찜하기 처리
    @PostMapping("/trade/like")
    @ResponseBody
    public Map<String, Object> tradeLike(@RequestParam long trade_seq, HttpSession session) {
        // 인터셉터가 로그인 검증 완료
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);

        boolean wished = tradeService.saveLike(trade_seq, member.getMember_seq());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("wished", wished);
        return result;
    }

    // 판매자 수동 sold 변경 API
    @PostMapping("/trade/sold")
    @ResponseBody
    public Map<String, Object> updateToSold(
            @RequestParam long trade_seq,
            HttpSession session
    ) {
        // 인터셉터가 로그인 검증 완료
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);

        // 판매자 권한 검증
        tradeService.validateSellerOwnership(trade_seq, member.getMember_seq());

        boolean success = tradeService.updateToSoldManually(trade_seq, member.getMember_seq());

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return result;
    }


    // 구매 확정 api
    @PostMapping("/trade/confirm/{trade_seq}")
    @ResponseBody
    public Map<String, Object> confirmPurchase(
            @PathVariable long trade_seq,
            HttpSession session
    ) {
        // 인터셉터가 로그인 검증 완료
        MemberVO member = (MemberVO) session.getAttribute(Const.SESSION);

        // 구매자 권한 검증
        tradeService.validateBuyerOwnership(trade_seq, member.getMember_seq());

        boolean success = tradeService.confirmPurchase(trade_seq, member.getMember_seq());

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return result;
    }

}
