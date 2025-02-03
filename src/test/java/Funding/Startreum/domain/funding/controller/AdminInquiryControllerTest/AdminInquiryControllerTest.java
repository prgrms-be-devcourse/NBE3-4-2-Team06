package Funding.Startreum.domain.funding.controller.AdminInquiryControllerTest;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.admin.inquiry.InquiryAdminService;
import Funding.Startreum.domain.admin.inquiry.InquiryListResponse;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminInquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

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

    @BeforeEach
    void setUp() {
        adminToken = jwtUtil.generateAccessToken(ADMIN_NAME, ADMIN_EMAIL, ADMIN_ROLE);
        userToken = jwtUtil.generateAccessToken(USER_NAME, USER_EMAIL, USER_ROLE);
    }

    @Test
    @DisplayName("관리자의 문의 목록 조회 성공 테스트")
    void getInquiries_AdminSuccess() throws Exception {

        LocalDateTime testTime = LocalDateTime.of(2025, 1, 24, 12, 0);
        var inquiryDataList = Arrays.asList(
                new InquiryListResponse.InquiryData(
                        1,
                        123,
                        "첫번째 문의 제목",
                        "첫번째 문의 내용입니다.",
                        "PENDING",
                        testTime,
                        testTime
                ),
                new InquiryListResponse.InquiryData(
                        2,
                        124,
                        "두번째 문의 제목",
                        "두번째 문의 내용입니다.",
                        "RESOLVED",
                        testTime,
                        testTime
                )
        );

        InquiryListResponse response = InquiryListResponse.success(inquiryDataList);
        when(inquiryAdminService.getInquiries(anyString())).thenReturn(response);

        ResultActions result = mockMvc.perform(get("/api/admin/inquiries")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("문의 목록 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data[0].inquiryId").value(1))
                .andExpect(jsonPath("$.data[0].userId").value(123))
                .andExpect(jsonPath("$.data[0].title").value("첫번째 문의 제목"))
                .andExpect(jsonPath("$.data[0].content").value("첫번째 문의 내용입니다."))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                .andExpect(jsonPath("$.data[1].inquiryId").value(2))
                .andExpect(jsonPath("$.data[1].status").value("RESOLVED"))
                .andDo(print());
    }

    @Test
    @DisplayName("일반 사용자의 문의 목록 조회 실패 테스트")
    void getInquiries_NonAdminFail() throws Exception {

        InquiryListResponse errorResponse = InquiryListResponse.error(403, "관리자 권한이 없습니다.");
        when(inquiryAdminService.getInquiries(anyString())).thenReturn(errorResponse);

        ResultActions result = mockMvc.perform(get("/api/admin/inquiries")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value("관리자 권한이 없습니다."))
                .andDo(print());
    }
}
