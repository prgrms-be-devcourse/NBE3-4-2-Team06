package Funding.Startreum.domain.virtualaccount.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        int transactionId,
        int accountId,
        BigDecimal chargeAmount,
        BigDecimal balance,
        LocalDateTime createdAt
) {
}

