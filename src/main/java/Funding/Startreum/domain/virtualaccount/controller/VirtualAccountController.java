package Funding.Startreum.domain.virtualaccount.controller;

import Funding.Startreum.domain.virtualaccount.dto.request.AccountRequest;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountResponse;
import Funding.Startreum.domain.virtualaccount.service.VirtualAccountService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class VirtualAccountController {

    private final VirtualAccountService service;

    // 잔액 충전
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{accountId}")
    public ResponseEntity<?> chargeVirtualAccount(
            @PathVariable(name = "accountId") int accountId,
            @RequestBody @Valid AccountRequest request
    ) {
        try {
            AccountResponse response = service.charge(accountId, request);
            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "message", "계좌 충전에 성공했습니다.",
                            "data", response
                    )
            );
        } catch (EntityNotFoundException e) {
            return ResponseEntity.ok(
                    Map.of(
                            "status", "error",
                            "message", "계좌를 찾을 수 없습니다."
                    )
            );
        }
    }

    // 거래 내역 조회
    @GetMapping("/{accountId}")
    public void getPayment(
            @PathVariable int accountId
    ) {
        System.out.println("거래 내역을 조회했습니다.");
    }

    // 결제
    @PostMapping("/{accountId}/charge")
    public void processPayment(
            @PathVariable int accountId
    ) {
        System.out.println("결제를 완료했습니다.");
    }

    // 환불
    @PostMapping("/{accountId}/transactions/{transactionId}/refund")
    public void processRefund(
            @PathVariable int accountId,
            @PathVariable int transactionId
    ) {
        System.out.println("환불을 완료했습니다.");
    }

}
