package Funding.Startreum.domain.users;

import Funding.Startreum.common.util.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
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

    // 로그인 API (JWT 발급)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("로그인 요청 받음: name=" + loginRequest.name());

            UserResponse user = userService.authenticateUser(loginRequest.name(), loginRequest.password());

            // Access Token 및 Refresh Token 생성 (role 포함)
            String accessToken = jwtUtil.generateAccessToken(user.name(), user.email(), user.role().name());
            String refreshToken = jwtUtil.generateRefreshToken(user.name());

            // Refresh Token 저장
            userService.saveRefreshToken(user.name(), refreshToken);

            // 응답 반환 (JWT, 사용자 이름, 역할 포함)
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("userName", user.name());
            response.put("role", user.role().name()); // 사용자 역할 추가

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "서버 내부 오류 발생"));
        }
    }

    // Access Token 갱신 (Refresh Token 사용)
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "유효하지 않은 Refresh Token"));
        }

        // Refresh Token에서 사용자 정보 추출
        String name = jwtUtil.getNameFromToken(refreshToken);

        // DB에서 저장된 Refresh Token과 비교
        String storedToken = userService.getRefreshToken(name);
        if (!refreshToken.equals(storedToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Refresh Token 불일치"));
        }

        // 사용자 정보 조회
        User user = userService.getUserByName(name);

        // 새 Access Token 생성 (role 포함)
        String newAccessToken = jwtUtil.generateAccessToken(user.getName(), user.getEmail(), user.getRole().name());

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }


    //  DTO 클래스 추가
    record LoginRequest(String name, String password) {}


}


