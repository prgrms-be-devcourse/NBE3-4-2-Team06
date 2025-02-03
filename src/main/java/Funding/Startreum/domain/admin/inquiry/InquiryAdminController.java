package Funding.Startreum.domain.admin.inquiry;

import Funding.Startreum.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class InquiryAdminController {

    private final InquiryAdminService inquiryAdminService;
    private final JwtUtil jwtUtil;

    @GetMapping("/inquiries")
    public ResponseEntity<InquiryListResponse> getInquiries(
            @RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        InquiryListResponse response = inquiryAdminService.getInquiries(email);
        return ResponseEntity.ok(response);
    }
}
