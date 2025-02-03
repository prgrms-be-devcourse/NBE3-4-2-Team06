package Funding.Startreum.domain.admin.inquiry;

import Funding.Startreum.domain.inquiry.Inquiry;
import Funding.Startreum.domain.inquiry.InquiryRepository;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryAdminService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Transactional
    public InquiryListResponse getInquiries(String email) {
        try {
            // 1. Verify admin authorization
            User admin = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            if (!admin.getRole().equals("ADMIN")) {
                return InquiryListResponse.error(403, "관리자 권한이 없습니다.");
            }

            // 2. Get all inquiries
            List<Inquiry> inquiries = inquiryRepository.findAllByOrderByCreatedAtDesc();

            // 3. Convert to response format
            List<InquiryListResponse.InquiryData> inquiryDataList = inquiries.stream()
                    .map(inquiry -> new InquiryListResponse.InquiryData(
                            inquiry.getInquiryId(),
                            inquiry.getUser().getUserId(),
                            inquiry.getTitle(),
                            inquiry.getContent(),
                            inquiry.getStatus().toString(),
                            inquiry.getCreatedAt(),
                            inquiry.getUpdatedAt()
                    ))
                    .collect(Collectors.toList());

            return InquiryListResponse.success(inquiryDataList);

        } catch (IllegalArgumentException e) {
            return InquiryListResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return InquiryListResponse.error(500, "문의 목록 조회 중 오류가 발생했습니다.");
        }
    }
}
