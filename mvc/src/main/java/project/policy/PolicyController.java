package project.policy;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PolicyController {

    @GetMapping("/privacy")
    public String privacyPolicy() {
        return "policy/privacy";
    }
}
