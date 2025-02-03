package Funding.Startreum.domain.admin.inquiry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class InquiryListResponse {
    private final String status;
    private final int statusCode;
    private final String message;
    private final List<InquiryData> data;

    private InquiryListResponse(String status, int statusCode, String message, List<InquiryData> data) {
        this.status = status;
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    public static InquiryListResponse success(List<InquiryData> data) {
        return new InquiryListResponse("ok", 200, "문의 목록 조회에 성공했습니다.", data);
    }

    public static InquiryListResponse error(int statusCode, String message) {
        return new InquiryListResponse("error", statusCode, message, null);
    }

    @Getter
    @RequiredArgsConstructor
    public static class InquiryData {
        private final Integer inquiryId;
        private final Integer userId;
        private final String title;
        private final String content;
        private final String status;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;
    }
}
