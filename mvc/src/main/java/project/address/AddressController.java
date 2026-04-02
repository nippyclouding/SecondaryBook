package project.address;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import project.member.MemberVO;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/profile/address")
public class AddressController {

    private final AddressService addressService;

    @GetMapping("/list")
    @ResponseBody
    public List<AddressVO> getAddressList(HttpSession sess) {
        MemberVO user = (MemberVO) sess.getAttribute("loginSess");
        if (user == null) return null;
        return addressService.getAddressList(user.getMember_seq());
    }

    @PostMapping("/add")
    @ResponseBody
    public String addAddress(@Valid AddressVO vo, BindingResult bindingResult, HttpSession sess) {
        MemberVO user = (MemberVO) sess.getAttribute("loginSess");
        if (user == null) return "fail";

        if (bindingResult.hasErrors()) {
            if (bindingResult.getFieldError("post_no") != null) {
                String code = bindingResult.getFieldError("post_no").getCode();
                if ("NotBlank".equals(code)) return "fail_post_no_required";
                if ("Pattern".equals(code)) return "fail_invalid_post_no";
            }
            return "fail";
        }

        int currentCount = addressService.getAddressCount(user.getMember_seq());
        if (currentCount >= 5) {
            return "count_limit"; // 제한 초과 시 별도의 메시지를 리턴
        }
        vo.setMember_seq(user.getMember_seq());

        boolean result = addressService.addAddress(vo);
        return result ? "success" : "fail";
    }

    // [추가] 수정 요청
    @PostMapping("/update")
    @ResponseBody
    public String updateAddress(@Valid AddressVO vo, BindingResult bindingResult, HttpSession sess) {
        MemberVO user = (MemberVO) sess.getAttribute("loginSess");
        if (user == null) return "fail";

        if (bindingResult.hasErrors()) {
            if (bindingResult.getFieldError("post_no") != null) {
                String code = bindingResult.getFieldError("post_no").getCode();
                if ("NotBlank".equals(code)) return "fail_post_no_required";
                if ("Pattern".equals(code)) return "fail_invalid_post_no";
            }
            return "fail";
        }

        vo.setMember_seq(user.getMember_seq());

        boolean result = addressService.updateAddress(vo);
        return result ? "success" : "fail";
    }

    @PostMapping("/delete")
    @ResponseBody
    public String deleteAddress(@RequestParam long addr_seq, HttpSession sess) {
        MemberVO user = (MemberVO) sess.getAttribute("loginSess");
        if (user == null) return "fail";

        boolean result = addressService.deleteAddress(addr_seq, user.getMember_seq());
        return result ? "success" : "fail";
    }

    @PostMapping("/setDefault")
    @ResponseBody
    public String setDefaultAddress(@RequestParam long addr_seq, HttpSession sess) {
        MemberVO user = (MemberVO) sess.getAttribute("loginSess");
        if (user == null) return "fail";

        boolean result = addressService.setMyDefaultAddress(user.getMember_seq(), addr_seq);
        return result ? "success" : "fail";
    }
}