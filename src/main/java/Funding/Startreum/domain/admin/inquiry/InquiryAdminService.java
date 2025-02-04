package Funding.Startreum.domain.admin.inquiry;

import Funding.Startreum.domain.inquiry.Inquiry;
import Funding.Startreum.domain.inquiry.InquiryRepository;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class InquiryAdminService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    /**
     * 문의 내역 확인용 메서드 - 관리자
     *
     *      * @param email
     *      * @return 문의 목록을 담은 InquiryListResponse 객체
     */

    @Transactional
    public InquiryListResponse getInquiries(@AuthenticationPrincipal User admin) {
        try {
            if (admin == null || !admin.getRole().equals("ADMIN")) {
                return InquiryListResponse.error(403, "관리자 권한이 없습니다.");
            }

            List<Inquiry> inquiries = inquiryRepository.findAllByOrderByCreatedAtDesc();

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

    /**
     * 관리자가 특정 문의에 대한 답변을 작성하는 메서드
     *
     * @param email      관리자 이메일
     * @param inquiryId  답변할 문의 ID
     * @param request    문의 답변 내용을 담고 있는 요청 객체
     * @return           처리 결과를 담은 InquiryAdminResponse 객체
     */

    @Transactional
    public InquiryAdminResponse replyToInquiry(String email, Long inquiryId, InquiryAdminRequest request) {
        try{
            // 이메일을 기반으로 사용자를 조회 (관리자인지 확인)
            User admin = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            if (!admin.getRole().equals("ADMIN")) {
                return InquiryAdminResponse.error(403, "해당 작업을 수행할 권한이 없습니다.");
            }

            // 문의 ID를 기반으로 문의 정보 조회
            Inquiry inquiry = inquiryRepository.findById(inquiryId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 문의를 찾을 수 없습니다."));

            // 문의에 대한 관리자 답변 설정
            inquiry.setAdminResponse(request.adminResponse());
            inquiry.setStatus(Inquiry.Status.RESOLVED);
            inquiry.setUpdatedAt(LocalDateTime.now());

            inquiry = inquiryRepository.save(inquiry);

            // 응답 데이터 생성
            var responseData = new InquiryAdminResponse.Data(
                    inquiry.getInquiryId(),
                    inquiry.getUser().getUserId(),
                    inquiry.getStatus().toString(),
                    inquiry.getAdminResponse(),
                    inquiry.getCreatedAt(),
                    inquiry.getUpdatedAt()
            );

            return InquiryAdminResponse.success(responseData);

        } catch (IllegalArgumentException e) {
            return InquiryAdminResponse.error(400, e.getMessage());
        } catch (Exception e) {
            return InquiryAdminResponse.error(500, "문의 처리하는 중 오류가 발생했습니다.");
        }
    }
}
