package Funding.Startreum.domain.transaction.entity;

import Funding.Startreum.domain.funding.Funding;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString(exclude = {"funding", "senderAccount", "receiverAccount"}) // 순환 참조 방지
@Entity
@Table(name = "Transaction")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId; // 거래 고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funding_id")
    private Funding funding; // 펀딩 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin; // 관리자 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id", nullable = false)
    private VirtualAccount senderAccount; // 송신 계좌

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_account_id", nullable = false)
    private VirtualAccount receiverAccount; // 수신 계좌

    private BigDecimal amount; // 거래 금액

    @Enumerated(EnumType.STRING)
    private TransactionType type; // 거래 유형

    private LocalDateTime transactionDate; // 거래 일자

    public enum TransactionType {
        REMITTANCE,  // 송금
        REFUND       // 환불
    }
}