package Funding.Startreum.domain.virtualaccount.dto.response;

import Funding.Startreum.domain.transaction.entity.Transaction;
import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountPaymentResponse(
        int transactionId,              // 진행된 거래 ID
        int accountId,                  // 계좌 ID
        BigDecimal beforeMoney,         // 계산 전 금액
        BigDecimal chargeAmount,        // 적용 금액
        BigDecimal afterMoney,          // 계산 후 금액
        LocalDateTime transactionDate   // 거래 일자
) {

    public static AccountPaymentResponse mapToAccountResponse(
            VirtualAccount account,
            Transaction transaction,
            BigDecimal beforeMoney,
            BigDecimal chargeAmount,
            BigDecimal afterMoney
    ) {
        return new AccountPaymentResponse(
                transaction.getTransactionId(),
                account.getAccountId(),
                beforeMoney,
                chargeAmount,
                afterMoney,
                transaction.getTransactionDate()
        );
    }

}

