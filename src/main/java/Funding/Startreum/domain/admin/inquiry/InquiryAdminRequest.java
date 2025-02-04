package Funding.Startreum.domain.admin.inquiry;

import jakarta.validation.constraints.NotBlank;

public record InquiryAdminRequest (
        @NotBlank(message = "관리자 응답은 필수입니다.")
        String adminResponse
) {}
