package Funding.Startreum.domain.virtualaccount.controller;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.users.CustomUserDetailsService;
import Funding.Startreum.domain.users.UserService;
import Funding.Startreum.domain.virtualaccount.dto.request.AccountRequest;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountPaymentResponse;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountResponse;
import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;
import Funding.Startreum.domain.virtualaccount.exception.AccountNotFoundException;
import Funding.Startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import Funding.Startreum.domain.virtualaccount.service.VirtualAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static Funding.Startreum.domain.users.User.Role.ADMIN;
import static Funding.Startreum.domain.users.User.Role.SPONSOR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private UserService userService;

    private String adminToken;              // 관리자 토큰
    private String ownerToken;              // 실제 계좌의 소유자 토큰
    private String notOwnerToken;           // 다른 계좌 소유자 토큰

    /**
     * 테스트 시작 전 계정 및 토큰을 설정합니다.
     */
    @BeforeEach
    void setUp() {
        // 가상 계좌 생성 (accountId: 100, 소유자: owner)
        createVirtualAccount(100, "owner");

        // 사용자 세부 정보 생성
        createVirtualDetails("admin", "ADMIN");
        createVirtualDetails("owner", "SPONSOR");
        createVirtualDetails("other", "SPONSOR");

        // 사용자 정보 설정 (userId, 이름, 역할)
        setVirutalUser(1, "admin", ADMIN);
        setVirutalUser(2, "owner", SPONSOR);
        setVirutalUser(3, "other", SPONSOR);

        // JWT 토큰 생성
        adminToken = jwtUtil.generateAccessToken("admin", "admin@test.com", "ADMIN");
        ownerToken = jwtUtil.generateAccessToken("owner", "owner@test.com", "SPONSOR");
        notOwnerToken = jwtUtil.generateAccessToken("other", "other@test.com", "SPONSOR");
    }

    /**
     * 가상 사용자 정보를 설정합니다.
     *
     * @param userId   사용자 ID
     * @param username 사용자 이름
     * @param role     사용자 역할
     */
    private void setVirutalUser(int userId, String username, Funding.Startreum.domain.users.User.Role role) {
        Funding.Startreum.domain.users.User ownerUser = new Funding.Startreum.domain.users.User();
        ownerUser.setUserId(userId);
        ownerUser.setName(username);
        ownerUser.setRole(role);

        given(userService.getUserByName(username)).willReturn(ownerUser);
    }

    /**
     * 가상 계좌 데이터를 설정하는 메서드입니다.
     *
     * @param accountId    계좌 ID
     * @param accountOwner 계좌 소유자
     */
    private void createVirtualAccount(int accountId, String accountOwner) {
        Funding.Startreum.domain.users.User user = new Funding.Startreum.domain.users.User();
        user.setName(accountOwner);
        VirtualAccount mockAccount = new VirtualAccount();
        mockAccount.setAccountId(accountId);
        mockAccount.setUser(user);
        mockAccount.setBalance(new BigDecimal("0.00"));

        given(virtualAccountRepository.findById(accountId))
                .willReturn(Optional.of(mockAccount));
    }

    /**
     * 가상 사용자 정보를 설정합니다.
     *
     * @param username 사용자 이름
     * @param role     사용자 역할 (ADMIN, SPONSOR, BENEFICIARY 등)
     */
    private void createVirtualDetails(String username, String role) {
        UserDetails adminUserDetails =
                User.builder()
                        .username(username)
                        .password("1234")
                        .roles(role)
                        .build();

        given(userDetailsService.loadUserByUsername(username))
                .willReturn(adminUserDetails);
    }

    // ===============================================================
    // 조회 관련 테스트
    // ===============================================================

    /**
     * ADMIN 계정으로 존재하는 계좌를 조회합니다.
     * 기대 결과: 200 OK
     */
    @Test
    @DisplayName("[조회 200] ADMIN 계정으로 계좌 조회 시")
    void getAccountTransactionsTest() throws Exception {
        int accountId = 100;
        AccountResponse response = new AccountResponse(accountId, BigDecimal.ZERO, LocalDateTime.now());

        given(virtualAccountService.getAccountInfo(accountId))
                .willReturn(response);

        mockMvc.perform(
                        get("/api/account/{accountId}", accountId)
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("계좌 내역 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.accountId").value(accountId));
    }

    /**
     * OWNER 계정으로 계좌를 조회합니다.
     * 기대 결과: 200 OK
     */
    @Test
    @DisplayName("[조회 200] OWNER 계정으로 계좌 조회 시")
    void getAccountTransactionsTest2() throws Exception {
        int accountId = 100;
        AccountResponse response = new AccountResponse(accountId, BigDecimal.ZERO, LocalDateTime.now());

        given(virtualAccountService.getAccountInfo(accountId))
                .willReturn(response);

        // When
        mockMvc.perform(
                        get("/api/account/{accountId}", accountId)
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("계좌 내역 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.accountId").value(accountId));
    }

    /**
     * OWNER 계정으로 존재하지 않는 계좌를 조회할 때 예외를 발생시킵니다.
     * 기대 결과: 404 Not Found
     */
    @Test
    @DisplayName("[조회 404] OWNER 계정으로 없는 계좌 조회 시")
    void getAccountTransactionsTest3() throws Exception {
        // Given
        int accountId = 500;
        given(virtualAccountService.getAccountInfo(accountId))
                .willThrow(new AccountNotFoundException(accountId));

        // When
        mockMvc.perform(
                        get("/api/account/{accountId}", accountId)
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )

                // Then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("해당 계좌를 찾을 수 없습니다 : " + accountId))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    /**
     * NOT OWNER 계정으로 다른 사람의 계좌를 조회할 때 예외를 발생시킵니다.
     * 기대 결과: 403 Forbidden
     */
    @Test
    @DisplayName("[조회 403] NOT OWNER 계정으로 OWNER 계좌 조회 시")
    void getAccountTransactionsTest4() throws Exception {
        // Given
        int accountId = 100;
        AccountResponse response = new AccountResponse(
                0,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );

        given(virtualAccountService.getAccountInfo(accountId))
                .willReturn(response);

        // When
        mockMvc.perform(
                        get("/api/account/{accountId}", accountId)
                                .header("Authorization", "Bearer " + notOwnerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )

                // Then
                .andExpect(status().isForbidden());
    }

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

        given(virtualAccountService.charge(eq(accountId), any(AccountRequest.class)))
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

        given(virtualAccountService.charge(eq(accountId), any(AccountRequest.class)))
                .willReturn(response);

        // When
        mockMvc.perform(
                        post("/api/account/{accountId}", accountId)
                                .header("Authorization", "Bearer " + ownerToken)
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

        given(virtualAccountService.charge(eq(accountId), any(AccountRequest.class)))
                .willThrow(new AccountNotFoundException(accountId));
        ;

        // When
        mockMvc.perform(
                        post("/api/account/{accountId}", accountId)
                                .header("Authorization", "Bearer " + ownerToken)
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

        given(virtualAccountService.charge(eq(accountId), any(AccountRequest.class)))
                .willReturn(response);

        // When
        mockMvc.perform(
                        post("/api/account/{accountId}", accountId)
                                .header("Authorization", "Bearer " + notOwnerToken)
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