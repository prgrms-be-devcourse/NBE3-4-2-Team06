package Funding.Startreum.domain.virtualaccount.controller;

import Funding.Startreum.domain.virtualaccount.service.VirtualAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class VirtualAccountController {

    private final VirtualAccountService service;

    // 잔액 충전
    @PostMapping("/{accountId}")
    public void chargeVirtualAccount(
            @PathVariable int accountId
    ){
        System.out.println("잔액 충전을 완료했습니다.");
    }

    // 거래 내역 조회
    @GetMapping("/{accountId}")
    public void getPayment(
            @PathVariable int accountId
    ){
        System.out.println("거래 내역을 조회했습니다.");
    }

    // 결제
    @PostMapping("/{accountId}/charge")
    public void processPayment(
            @PathVariable int accountId
    ){
        System.out.println("결제를 완료했습니다.");
    }

    // 환불
    @PostMapping("/{accountId}/transactions/{transactionId}/refund")
    public void processRefund(
            @PathVariable int accountId,
            @PathVariable int transactionId
    ){
        System.out.println("환불을 완료했습니다.");
    }

}
