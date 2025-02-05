package Funding.Startreum.domain.admin.inquiry;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class InquiryAdminController {

    private final InquiryAdminService inquiryAdminService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @GetMapping("/inquiries")
    public ResponseEntity<InquiryListResponse> getInquiries(
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        InquiryListResponse response = inquiryAdminService.getInquiries(admin);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/inquiries/{inquiryId}")
    public ResponseEntity<InquiryAdminResponse> replyToInquiry(
            @RequestHeader("Authorization") String token,
            @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryAdminRequest request) {

        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        InquiryAdminResponse response = inquiryAdminService.replyToInquiry(email, inquiryId, request);
        return ResponseEntity.ok(response);
    }
}