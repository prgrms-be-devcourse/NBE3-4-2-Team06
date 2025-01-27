package Funding.Startreum.domain.users;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserViewController {

    // 회원가입 페이지 호출
    @GetMapping("api/users/signup")
    public String showSignupForm(Model model) {
        // 필요 시 모델에 데이터 추가 가능
        return "users/signup"; // templates/users/signup.html
    }

    // 로그인 페이지 호출
    @GetMapping("api/users/login")
    public String showloginForm(Model model) {
        // 필요 시 모델에 데이터 추가 가능
        return "users/login";
    }
}