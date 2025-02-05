package Funding.Startreum.domain.virtualaccount.controller;

import Funding.Startreum.domain.virtualaccount.dto.VirtualAccountDtos;
import Funding.Startreum.domain.virtualaccount.service.VirtualAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class VirtualAccountController {

    private final VirtualAccountService service;

    /**
     * 특정 사용자의 계좌 조회 API (이름 기반)
     */
    @GetMapping("/user/{name}")
    public ResponseEntity<VirtualAccountDtos> getAccount(@PathVariable String name, Principal principal) {
        System.out.println("🔍 Principal 정보: " + (principal != null ? principal.getName() : "NULL"));
        System.out.println("🔍 요청된 사용자: " + name);

        if (principal == null) {
            System.out.println("❌ 인증되지 않은 사용자 요청");
            return ResponseEntity.status(401).body(new VirtualAccountDtos(false)); // Unauthorized
        }

        if (!principal.getName().equals(name)) {
            System.out.println("❌ 본인 또는 관리자가 아님: 접근 불가");
            return ResponseEntity.status(403).body(new VirtualAccountDtos(false)); // Forbidden
        }

        VirtualAccountDtos account = service.findByName(name);
        return ResponseEntity.ok().body(account);
    }

    /**
     * 계좌 생성 API
     */
    @PostMapping("/user/{name}/create")
    public ResponseEntity<VirtualAccountDtos> createAccount(@PathVariable String name, Principal principal) {
        if (principal == null || !principal.getName().equals(name)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new VirtualAccountDtos(false));  // ✅ HttpStatus.FORBIDDEN 사용
        }

        try {
            VirtualAccountDtos newAccount = service.createAccount(name);
            return ResponseEntity.ok().body(newAccount);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(new VirtualAccountDtos(false));
        }
    }




    /**
     * 잔액 충전 API
     */
    @PostMapping("/{accountId}")
    public ResponseEntity<String> chargeVirtualAccount(@PathVariable int accountId) {
        System.out.println("잔액 충전을 완료했습니다.");
        return ResponseEntity.ok("잔액 충전 완료");
    }

    /**
     * 거래 내역 조회 API
     */
    @GetMapping("/{accountId}")
    public ResponseEntity<String> getPayment(@PathVariable int accountId) {
        System.out.println("거래 내역을 조회했습니다.");
        return ResponseEntity.ok("거래 내역 조회 완료");
    }

    /**
     * 결제 API
     */
    @PostMapping("/{accountId}/charge")
    public ResponseEntity<String> processPayment(@PathVariable int accountId) {
        System.out.println("결제를 완료했습니다.");
        return ResponseEntity.ok("결제 완료");
    }

    /**
     * 환불 API
     */
    @PostMapping("/{accountId}/transactions/{transactionId}/refund")
    public ResponseEntity<String> processRefund(
            @PathVariable int accountId,
            @PathVariable int transactionId) {
        System.out.println("환불을 완료했습니다.");
        return ResponseEntity.ok("환불 완료");
    }
}