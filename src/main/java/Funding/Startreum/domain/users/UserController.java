package Funding.Startreum.domain.users;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ID 중복 확인
    @GetMapping("/check-name")
    public ResponseEntity<Boolean> checkNameDuplicate(@RequestParam String name) {
        boolean isDuplicate = userService.isNameDuplicate(name);
        return ResponseEntity.ok(isDuplicate);
    }

    // 이메일 중복 확인
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String email) {
        boolean isDuplicate = userService.isEmailDuplicate(email);
        return ResponseEntity.ok(isDuplicate);
    }
    // 회원가입 처리 (REST API)
    @PostMapping("/registrar")
    public ResponseEntity<?> registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam User.Role role) {

        SignupRequest signupRequest = new SignupRequest(name, email, password, role);
        userService.registerUser(signupRequest);

        // 메인 페이지로 리디렉션
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

}