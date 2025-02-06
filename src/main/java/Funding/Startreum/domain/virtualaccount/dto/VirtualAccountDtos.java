package Funding.Startreum.domain.virtualaccount.dto;

import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirtualAccountDtos {

    private boolean accountExists; // 계좌 존재 여부
    private String username; // 사용자 이름
    private BigDecimal balance; // 잔액
    private LocalDateTime createdAt; // 계좌 생성 날짜
    private Boolean fundingBlocked; // 펀딩 차단 여부

    // 계좌가 없는 경우를 위한 생성자
    public VirtualAccountDtos(boolean accountExists) {
        this.accountExists = accountExists;
    }

    public VirtualAccountDtos(VirtualAccount account) {
        this.accountExists = true;
        this.username = account.getUser().getName();
        this.balance = account.getBalance();
        this.createdAt = account.getCreatedAt();
        this.fundingBlocked = account.getFundingBlock();
    }

    public static VirtualAccountDtos fromEntity(VirtualAccount account) {
        return VirtualAccountDtos.builder()
                .accountExists(true)
                .username(account.getUser().getName())
                .balance(account.getBalance())
                .createdAt(account.getCreatedAt())
                .fundingBlocked(account.getFundingBlock())
                .build();
    }
}