package Funding.Startreum.domain.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RemittanceResponseDto(
        String status,
        int statusCode,
        String message,
        Integer projectId,
        List<RemittanceDetail> remittanceDetails
) {
    public record RemittanceDetail(
            int fundingId,
            int adminId,
            int sendedAccountId,
            int receiverAccountId,
            BigDecimal amount,
            String transactionCode,
            LocalDateTime transactionDate
    ) {}
}
