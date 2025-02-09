package Funding.Startreum.domain.virtualaccount.controller;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.project.repository.ProjectRepository;
import Funding.Startreum.domain.users.CustomUserDetailsService;
import Funding.Startreum.domain.users.UserService;
import Funding.Startreum.domain.virtualaccount.dto.request.AccountRequest;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountPaymentResponse;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountRefundResponse;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountResponse;
import Funding.Startreum.domain.virtualaccount.exception.AccountNotFoundException;
import Funding.Startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import Funding.Startreum.domain.virtualaccount.service.VirtualAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static Funding.Startreum.util.TokenUtil.createUserToken;
import static Funding.Startreum.util.utilMethod.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


// TODO 수정 및 개선 필요
@SpringBootTest
@AutoConfigureMockMvc
class VirtualAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private VirtualAccountRepository virtualAccountRepository;

    @MockitoBean
    private VirtualAccountService virtualAccountService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private UserService userService;

    private String adminToken;                         // 관리자 토큰
    private String ownerTokenWithSponsor;              // 실제 계좌의 소유자 토큰
    private String notOwnerTokenWithSponsor;           // 다른 계좌 소유자 토큰
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String BASE_URL = "/api/account";
    private final Integer PROJECT_ID = 1;
    private final String OWNER = "owner";
    private final String ADMIN_NAME = "admin";
    private final String OTHER = "other";
    private final Integer ACCOUNT_ID = 100;

    /**
     * 테스트 시작 전 계정 및 토큰을 설정합니다.
     */
    @BeforeEach
    void setUp() {

        // 가상 계좌 및 프로젝트 생성
        createVirtualAccount(virtualAccountRepository, ACCOUNT_ID, OWNER);
        createVirtualProject(projectRepository, PROJECT_ID, OWNER);

        // 사용자 세부 정보 생성
        createVirtualDetails(userDetailsService, ADMIN_NAME, "ADMIN");
        createVirtualDetails(userDetailsService, OWNER, "SPONSOR");
        createVirtualDetails(userDetailsService, OTHER, "SPONSOR");

        // 사용자 정보 설정 (userId, 이름, 역할)
        setVirtualUser(userService, 1, ADMIN_NAME, Funding.Startreum.domain.users.User.Role.ADMIN);
        setVirtualUser(userService, 2, OWNER, Funding.Startreum.domain.users.User.Role.SPONSOR);
        setVirtualUser(userService, 3, OTHER, Funding.Startreum.domain.users.User.Role.SPONSOR);

        // JWT 토큰 생성
        adminToken = createUserToken(jwtUtil, ADMIN_NAME, "admin@test.com", "ADMIN");
        ownerTokenWithSponsor = createUserToken(jwtUtil, OWNER, "owner@test.com", "SPONSOR");
        notOwnerTokenWithSponsor = createUserToken(jwtUtil, OTHER, "other@test.com", "SPONSOR");
    }


    // ===============================================================
    // 조회 관련 테스트
    // ===============================================================

    @Nested
    @DisplayName("계좌 조회 테스트")
    class AccountInquiryTests {

        @Test
        @DisplayName("[조회 200] ADMIN 계정으로 계좌 조회 시")
        void getAccountTransactionsTest() throws Exception {
            int accountId = 100;
            AccountResponse response = new AccountResponse(accountId, BigDecimal.ZERO, LocalDateTime.now());

            given(virtualAccountService.getAccountInfo(accountId))
                    .willReturn(response);

            mockMvc.perform(
                            get("/api/account/{accountId}", accountId)
                                    .header("Authorization", "Bearer " + adminToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 내역 조회에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(accountId));
        }

        @Test
        @DisplayName("[조회 200] OWNER 계정으로 계좌 조회 시")
        void getAccountTransactionsTest2() throws Exception {
            int accountId = 100;
            AccountResponse response = new AccountResponse(accountId, BigDecimal.ZERO, LocalDateTime.now());

            given(virtualAccountService.getAccountInfo(accountId))
                    .willReturn(response);

            mockMvc.perform(
                            get("/api/account/{accountId}", accountId)
                                    .header("Authorization", "Bearer " + ownerTokenWithSponsor)
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 내역 조회에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(accountId));
        }

        @Test
        @DisplayName("[조회 404] OWNER 계정으로 없는 계좌 조회 시")
        void getAccountTransactionsTest3() throws Exception {
            int accountId = 500;
            given(virtualAccountService.getAccountInfo(accountId))
                    .willThrow(new AccountNotFoundException(accountId));

            mockMvc.perform(
                            get("/api/account/{accountId}", accountId)
                                    .header("Authorization", "Bearer " + ownerTokenWithSponsor)
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("해당 계좌를 찾을 수 없습니다 : " + accountId))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("[조회 403] NOT OWNER 계정으로 OWNER 계좌 조회 시")
        void getAccountTransactionsTest4() throws Exception {
            int accountId = 100;
            AccountResponse response = new AccountResponse(
                    0,
                    BigDecimal.ZERO,
                    LocalDateTime.now()
            );

            given(virtualAccountService.getAccountInfo(accountId))
                    .willReturn(response);

            mockMvc.perform(
                            get("/api/account/{accountId}", accountId)
                                    .header("Authorization", "Bearer " + notOwnerTokenWithSponsor)
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isForbidden());
        }
    }

    // ===============================================================
    // 충전 관련 테스트
    // ===============================================================

    @Nested
    @DisplayName("계좌 충전 테스트")
    class AccountChargeTests {
        /**
         * ADMIN 계정으로 존재하는 계좌에 잔액을 충전합니다.
         * 기대 결과: 200 OK
         */
        @Test
        @DisplayName("[충전 200] ADMIN 계정으로 존재하는 계좌 충전 시")
        void chargeAccountTest1() throws Exception {
            // Given
            int accountId = 100;
            BigDecimal amount = BigDecimal.valueOf(1000);

            AccountRequest request = new AccountRequest(amount);

            AccountPaymentResponse response = new AccountPaymentResponse(
                    0, accountId, amount, amount, amount, LocalDateTime.now()
            );

            given(virtualAccountService.chargeByAccountId(eq(accountId), any(AccountRequest.class)))
                    .willReturn(response);

            // When
            mockMvc.perform(
                            post("/api/account/{accountId}", accountId)
                                    .header("Authorization", "Bearer " + adminToken)
                                    .content("""
                                            {
                                                "amount": 1000
                                            }
                                            """.stripIndent())
                                    .contentType(MediaType.APPLICATION_JSON)

                    )

                    // Then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 충전에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(accountId))
                    .andExpect(jsonPath("$.data.afterMoney").value(1000));
        }

        /**
         * OWNER 계정으로 존재하는 계좌에 잔액을 충전합니다.
         * 기대 결과: 200 OK
         */
        @Test
        @DisplayName("[충전 200] OWNER 계정으로 존재하는 계좌 충전 시")
        void chargeAccountTest2() throws Exception {
            // Given
            int accountId = 100;
            BigDecimal amount = BigDecimal.valueOf(1000);
            AccountPaymentResponse response = new AccountPaymentResponse(
                    0, accountId, amount, amount, amount, LocalDateTime.now()
            );

            given(virtualAccountService.chargeByAccountId(eq(accountId), any(AccountRequest.class)))
                    .willReturn(response);

            // When
            mockMvc.perform(
                            post("/api/account/{accountId}", accountId)
                                    .header("Authorization", "Bearer " + ownerTokenWithSponsor)
                                    .content("""
                                            {
                                                "amount": 1000
                                            }
                                            """.stripIndent())
                                    .contentType(MediaType.APPLICATION_JSON)

                    )

                    // Then
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("계좌 충전에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(accountId))
                    .andExpect(jsonPath("$.data.afterMoney").value(1000));
        }

        /**
         * OWNER 계정으로 존재하지 않는 계좌에 잔액을 충전합니다.
         * 기대 결과: 404 NOT FOUND
         */
        @Test
        @DisplayName("[충전 404] OWNER 계정으로 존재하지 않는 계좌 충전 시")
        void chargeAccountTest3() throws Exception {
            // Given
            int accountId = 500;

            given(virtualAccountService.chargeByAccountId(eq(accountId), any(AccountRequest.class)))
                    .willThrow(new AccountNotFoundException(accountId));
            ;

            // When
            mockMvc.perform(
                            post("/api/account/{accountId}", accountId)
                                    .header("Authorization", "Bearer " + ownerTokenWithSponsor)
                                    .content("""
                                            {
                                                "amount": 1000
                                            }
                                            """.stripIndent())
                                    .contentType(MediaType.APPLICATION_JSON)

                    )

                    // Then
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("error"))
                    .andExpect(jsonPath("$.message").value("해당 계좌를 찾을 수 없습니다 : " + accountId))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        /**
         * NOT OWNER 계정으로 권힌이 없는 계좌에 잔액을 충전합니다.
         * 기대 결과: 403 FORBIDDEN
         */
        @Test
        @DisplayName("[충전 403] NOT OWNER 계정으로 OWNER 계좌 충전 시")
        void chargeAccountTest4() throws Exception {
            // Given
            int accountId = 100;
            BigDecimal amount = BigDecimal.valueOf(1000);
            AccountPaymentResponse response = new AccountPaymentResponse(
                    0, accountId, amount, amount, amount, LocalDateTime.now()
            );

            given(virtualAccountService.chargeByAccountId(eq(accountId), any(AccountRequest.class)))
                    .willReturn(response);

            // When
            mockMvc.perform(
                            post("/api/account/{accountId}", accountId)
                                    .header("Authorization", "Bearer " + notOwnerTokenWithSponsor)
                                    .content("""
                                            {
                                                "amount": 1000
                                            }
                                            """.stripIndent())
                                    .contentType(MediaType.APPLICATION_JSON)

                    )

                    // Then
                    .andExpect(status().isForbidden());
        }
    }

    // ===============================================================
    // 결제 관련 테스트
    // ===============================================================

    @Nested
    @DisplayName("계좌 결제 테스트")
    class AccountPaymentTests {
        /**
         * [결제 200] OWNER 계정으로 계좌 결제 시
         */
        @Test
        @DisplayName("[결제 200] OWNER 계정으로 계좌 결제 시")
        void processPaymentByAccountIdTest() throws Exception {
            int accountId = 100;
            BigDecimal amount = BigDecimal.valueOf(1000);

            AccountPaymentResponse response = new AccountPaymentResponse(
                    0, accountId, amount, amount, amount, LocalDateTime.now()
            );

            // 결제 요청이 들어왔을 때, 서비스 레이어가 해당 응답을 반환하도록 모킹합니다.
            given(virtualAccountService.payment(eq(accountId), any(), eq(OWNER)))
                    .willReturn(response);

            mockMvc.perform(
                            post("/api/account/{accountId}/payment", accountId)
                                    .header("Authorization", "Bearer " + ownerTokenWithSponsor)
                                    .content("""
                                            {
                                                "projectId": 1,
                                                "amount": 1000
                                            }
                                            """.stripIndent())
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("결제에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(accountId))
                    .andExpect(jsonPath("$.data.afterMoney").value(1000));
        }

        /**
         * [결제 200] 로그인한 사용자 계정으로 결제 시 (username 기반)
         */
        @Test
        @DisplayName("[결제 200] 로그인한 사용자 계정으로 결제 시")
        void processPaymentByUserNameTest() throws Exception {
            int accountId = 100;  // 실제로는 계좌 ID가 응답 데이터에 포함됩니다.
            BigDecimal amount = BigDecimal.valueOf(1000);

            AccountPaymentResponse response = new AccountPaymentResponse(
                    0, accountId, amount, amount, amount, LocalDateTime.now()
            );

            // username 기반 결제의 경우, 서비스 메서드의 시그니처는 payment(AccountPaymentRequest, username)입니다.
            given(virtualAccountService.payment(any(), eq(OWNER)))
                    .willReturn(response);

            mockMvc.perform(
                            post("/api/account/payment")
                                    .header("Authorization", "Bearer " + ownerTokenWithSponsor)
                                    .content("""
                                            {
                                                "projectId": 1,
                                                "amount": 1000
                                            }
                                            """.stripIndent())
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("결제에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(accountId))
                    .andExpect(jsonPath("$.data.afterMoney").value(1000));
        }

        /**
         * [결제 403] NOT OWNER 계정으로 OWNER 계좌 결제 시 (권한 없음)
         */
        @Test
        @DisplayName("[결제 403] NOT OWNER 계정으로 OWNER 계좌 결제 시")
        void processPaymentNotOwnerTest() throws Exception {
            int accountId = 100;
            // 결제 요청 시, NOT OWNER 계정은 접근 권한이 없으므로 서비스 호출 전 필터에서 403이 발생해야 합니다.
            mockMvc.perform(
                            post("/api/account/{accountId}/payment", accountId)
                                    .header("Authorization", "Bearer " + notOwnerTokenWithSponsor)
                                    .content("""
                                            {
                                                "projectId": 1,
                                                "amount": 1000
                                            }
                                            """.stripIndent())
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isForbidden());
        }
    }

    // ===============================================================
    // 환불 관련 테스트
    // ===============================================================

    @Nested
    @DisplayName("계좌 환불 테스트")
    class AccountRefundTests {
        /**
         * [환불 200] OWNER 계정으로 거래 환불 시
         */
        @Test
        @DisplayName("[환불 200] OWNER 계정으로 거래 환불 시")
        void processRefundTest() throws Exception {
            int accountId = 100;
            int transactionOriginalId = 200;
            int transactionRefundId = 200;
            BigDecimal refundAmount = BigDecimal.valueOf(1000);

            // AccountRefundResponse의 생성자: (transactionId, accountId, refundAmount, beforeMoney, afterMoney, transactionDate)
            AccountRefundResponse refundResponse = new AccountRefundResponse(
                    transactionRefundId, transactionOriginalId, accountId, refundAmount, BigDecimal.ZERO, refundAmount, LocalDateTime.now()
            );

            given(virtualAccountService.refund(accountId, transactionRefundId))
                    .willReturn(refundResponse);

            mockMvc.perform(
                            post("/api/account/{accountId}/transactions/{transactionId}/refund", accountId, transactionRefundId)
                                    .header("Authorization", "Bearer " + ownerTokenWithSponsor)
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("거래 환불에 성공했습니다."))
                    .andExpect(jsonPath("$.data.accountId").value(accountId))
                    .andExpect(jsonPath("$.data.refundTransactionId").value(transactionRefundId))
                    .andExpect(jsonPath("$.data.originalTransactionId").value(transactionOriginalId))
                    .andExpect(jsonPath("$.data.afterMoney").value(refundAmount.intValue()));
        }

        /**
         * [환불 403] NOT OWNER 계정으로 거래 환불 시 (권한 없음)
         */
        @Test
        @DisplayName("[환불 403] NOT OWNER 계정으로 거래 환불 시")
        void processRefundNotOwnerTest() throws Exception {
            int accountId = 100;
            int transactionId = 200;

            // NOT OWNER 계정으로 요청하면, PreAuthorize에 의해 403 Forbidden 응답을 받게 됩니다.
            mockMvc.perform(
                            post("/api/account/{accountId}/transactions/{transactionId}/refund", accountId, transactionId)
                                    .header("Authorization", "Bearer " + notOwnerTokenWithSponsor)
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isForbidden());
        }
    }

}