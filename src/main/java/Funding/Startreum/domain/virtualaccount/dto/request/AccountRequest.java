package Funding.Startreum.domain.virtualaccount.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountRequest(
        @NotNull(message = "계좌 충전에 실패했습니다. 금액을 확인해주세요.")
        @Min(value = 1, message = "계좌 충전에 실패했습니다. 금액을 확인해주세요.")
        BigDecimal chargeAmount
) {
}

