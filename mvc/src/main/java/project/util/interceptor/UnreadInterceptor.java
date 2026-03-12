package project.util.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import project.chat.message.MessageService;
import project.member.MemberVO;
import project.util.Const;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
@Component
public class UnreadInterceptor implements HandlerInterceptor {

    private final MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            MemberVO member = (MemberVO) request.getSession().getAttribute(Const.SESSION);
            if (member != null && messageService.isUnreadMessage(member.getMember_seq())) {
                modelAndView.addObject("messageSign", true);
            }
        }
    }
}
