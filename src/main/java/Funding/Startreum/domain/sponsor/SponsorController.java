package Funding.Startreum.domain.sponsor;

import Funding.Startreum.common.util.JwtUtil;
import io.jsonwebtoken.Jwt;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SponsorController {

    private final SponsorService sponsorService;
    private final JwtUtil jwtUtil;
    @GetMapping("/sponsor")
    public ResponseEntity<SponListResponse> getFundingList(
            @RequestHeader("Authorization") String token,
            @PageableDefault(size = 5) Pageable pageable) {

        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        SponListResponse response = sponsorService.getFundingList(email, pageable);
        return ResponseEntity.ok(response);
    }
}
