package Funding.Startreum.domain.funding.controller.InquiryAdminReplyControllerTest;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.admin.inquiry.InquiryAdminRequest;
import Funding.Startreum.domain.admin.inquiry.InquiryAdminResponse;
import Funding.Startreum.domain.admin.inquiry.InquiryAdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class InquiryAdminReplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InquiryAdminService inquiryAdminService;

    private String adminToken;
    private String userToken;
    private static final String ADMIN_NAME = "testAdmin";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_NAME = "testUser";
    private static final String USER_EMAIL = "user@test.com";
    private static final String USER_ROLE = "USER";
    private static final Integer TEST_INQUIRY_ID = 2;
    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2025, 2, 1, 12, 0);

    @BeforeEach
    void setUp() {
        adminToken = jwtUtil.generateAccessToken(ADMIN_NAME, ADMIN_EMAIL, ADMIN_ROLE);
        userToken = jwtUtil.generateAccessToken(USER_NAME, USER_EMAIL, USER_ROLE);
    }

    @Test
    @DisplayName("관리자의 문의 답변 성공 테스트")
    void replyToInquiry_AdminSuccess() throws Exception {
        // Given
        InquiryAdminRequest request = new InquiryAdminRequest("관리자 응답입니다");

        var responseData = new InquiryAdminResponse.Data(
                TEST_INQUIRY_ID,
                113,
                "RESOLVED",
                "관리자 응답입니다",
                TEST_TIME,
                TEST_TIME
        );

        InquiryAdminResponse response = InquiryAdminResponse.success(responseData);

        when(inquiryAdminService.replyToInquiry(anyString(), any(), any()))
                .thenReturn(response);

        // When
        ResultActions result = mockMvc.perform(patch("/api/admin/inquiries/" + TEST_INQUIRY_ID)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("문의 처리에 성공했습니다."))
                .andExpect(jsonPath("$.data.inquiryId").value(TEST_INQUIRY_ID))
                .andExpect(jsonPath("$.data.userId").value(113))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"))
                .andExpect(jsonPath("$.data.adminResponse").value("관리자 응답입니다"))
                .andDo(print());
    }


    @Test
    @DisplayName("일반 사용자의 문의 답변 실패 테스트")
    void replyToInquiry_NonAdminFail() throws Exception {
        // Given
        InquiryAdminRequest request = new InquiryAdminRequest("관리자 응답입니다");

        InquiryAdminResponse errorResponse = InquiryAdminResponse.error(403, "해당 작업을 수행할 권한이 없습니다.");

        when(inquiryAdminService.replyToInquiry(anyString(), any(), any()))
                .thenReturn(errorResponse);

        // When
        ResultActions result = mockMvc.perform(patch("/api/admin/inquiries/" + TEST_INQUIRY_ID)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value("해당 작업을 수행할 권한이 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("잘못된 문의 ID로 답변 실패 테스트")
    void replyToInquiry_InvalidInquiryIdFail() throws Exception {
        // Given
        InquiryAdminRequest request = new InquiryAdminRequest("관리자 응답입니다");

        InquiryAdminResponse errorResponse = InquiryAdminResponse.error(404, "해당 문의를 찾을 수 없습니다.");

        when(inquiryAdminService.replyToInquiry(anyString(), any(), any()))
                .thenReturn(errorResponse);

        // When
        ResultActions result = mockMvc.perform(patch("/api/admin/inquiries/9999")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(404))
                .andExpect(jsonPath("$.message").value("해당 문의를 찾을 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("빈 응답으로 문의 답변 실패 테스트")
    void replyToInquiry_EmptyResponseFail() throws Exception {
        // When
        ResultActions result = mockMvc.perform(patch("/api/admin/inquiries/" + TEST_INQUIRY_ID)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new InquiryAdminRequest(""))));

        // Then
        result.andExpect(status().isBadRequest())
                .andDo(print());
    }
}
