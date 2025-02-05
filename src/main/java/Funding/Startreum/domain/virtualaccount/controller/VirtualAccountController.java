package Funding.Startreum.domain.virtualaccount.controller;

import Funding.Startreum.common.util.ApiResponse;
import Funding.Startreum.domain.virtualaccount.dto.request.AccountRequest;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountResponse;
import Funding.Startreum.domain.virtualaccount.service.VirtualAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class VirtualAccountController {

    private final VirtualAccountService service;

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
