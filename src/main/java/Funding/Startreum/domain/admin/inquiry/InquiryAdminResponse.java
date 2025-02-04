package Funding.Startreum.domain.admin.inquiry;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InquiryAdminResponse {
    private final String status;
    private final int statusCode;
    private final String message;
    private final Data data;

    private InquiryAdminResponse(String status, int statusCode, String message, Data data) {
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public static InquiryAdminResponse success(Data data) {
        return new InquiryAdminResponse("success", 200, "문의 처리에 성공했습니다.", data);
    }

    public static InquiryAdminResponse error(int statusCode, String message) {
        return new InquiryAdminResponse("error", statusCode, message, null);
    }

    public record Data(
            Integer inquiryId,
            Integer userId,
            String status,
            String adminResponse,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
    }
}
