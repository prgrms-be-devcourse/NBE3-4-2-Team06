package Funding.Startreum.domain.transaction.controller;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.transaction.dto.RemittanceResponseDto;
import Funding.Startreum.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/projects")
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtUtil jwtUtil;

    // ✅ 펀딩 성공 프로젝트에 대한 송금 처리
    @PostMapping("/remit/{projectId}")
    @PreAuthorize("hasRole('ADMIN')") // 관리자는 ROLE_ADMIN
    public ResponseEntity<RemittanceResponseDto> processRemittance(@PathVariable Integer projectId, @RequestHeader("Authorization") String token) {
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        RemittanceResponseDto response = transactionService.processRemittance(projectId, email);
        return ResponseEntity.ok(response);
    }
}
