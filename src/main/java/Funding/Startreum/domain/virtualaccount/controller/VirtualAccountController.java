package Funding.Startreum.domain.virtualaccount.controller;


import Funding.Startreum.common.util.ApiResponse;
import Funding.Startreum.domain.virtualaccount.dto.VirtualAccountDtos;
import Funding.Startreum.domain.virtualaccount.dto.request.AccountRequest;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountResponse;
import Funding.Startreum.domain.virtualaccount.service.VirtualAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
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


    // 잔액 충전: 계좌에 금액을 충전합니다. 해당 계좌의 본인과 관리자만 가능합니다.
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.isAccountOwner(principal, #accountId)")
    @PostMapping("/{accountId}")
    public ResponseEntity<?> chargeVirtualAccount(
            @PathVariable(name = "accountId") @P("accountId") int accountId,
            @RequestBody @Valid AccountRequest request
    ) {
        AccountResponse response = service.charge(accountId, request);
        return ResponseEntity.ok(ApiResponse.success("계좌 충전에 성공했습니다.", response));
    }

    // 거래 내역 조회: 특정 계좌의 거래 내역을 조회합니다. 해당 계좌의 본인과 관리자만 가능합니다.
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.isAccountOwner(principal, #accountId)")
    @GetMapping("/{accountId}")
    public ResponseEntity<?> getAccountTransactions(
            @PathVariable("accountId") @P("accountId") int accountId
    ) {
        AccountResponse response = service.getAccountInfo(accountId);
        return ResponseEntity.ok(ApiResponse.success("계좌 내역 조회에 성공했습니다.", response));
    }

    // 결제 처리: 결제 요청을 처리합니다.
    @PostMapping("/{accountId}/charge")
    public void processPayment(
            @PathVariable int accountId
    ) {
        System.out.println("결제를 완료했습니다.");
    }

    // 환불 처리: 특정 거래에 대한 환불을 진행합니다.
    @PostMapping("/{accountId}/transactions/{transactionId}/refund")
    public void processRefund(
            @PathVariable int accountId,
            @PathVariable int transactionId
    ) {
        System.out.println("환불을 완료했습니다.");
    }

}