package Funding.Startreum.domain.virtualaccount.controller;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.users.CustomUserDetailsService;
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

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private String adminToken;            // 관리자
    private String ownerToken;     // 실제 계좌의 소유자
    private String notOwnerToken;  // 다른 계좌 소유자

    @BeforeEach
    void setUp() {
        createAccount(100, "owner");
        // createAccount(200, "owner");

        createDetails("admin", "ADMIN");
        createDetails("owner", "SPONSOR");
        createDetails("other", "SPONSOR");

        this.adminToken = jwtUtil.generateAccessToken("admin", "admin@test.com", "ADMIN");
        this.ownerToken = jwtUtil.generateAccessToken("owner", "owner@test.com", "SPONSOR");
        this.notOwnerToken = jwtUtil.generateAccessToken("other", "other@test.com", "SPONSOR");
    }

    private void createAccount(int accountId, String accountOwner) {
        Funding.Startreum.domain.users.User user = new Funding.Startreum.domain.users.User();
        user.setName(accountOwner);
        VirtualAccount mockAccount = new VirtualAccount();
        mockAccount.setAccountId(accountId);
        mockAccount.setUser(user);
        given(virtualAccountRepository.findById(accountId))
                .willReturn(Optional.of(mockAccount));
    }

    private void createDetails(String adminUser, String ROLE) {
        UserDetails adminUserDetails =
                User.builder()
                        .username(adminUser)
                        .password("1234")
                        .roles(ROLE)
                        .build();

        given(userDetailsService.loadUserByUsername(adminUser))
                .willReturn(adminUserDetails);
    }

    @Test
    @DisplayName("[조회 200] ADMIN 계정으로 계좌 조회 시")
    void getAccountTransactionsTest() throws Exception {
        // Given
        int accountId = 100;
        AccountResponse response = new AccountResponse(
                0,
                accountId,
                BigDecimal.TEN,
                BigDecimal.ONE,
                LocalDateTime.now()
        );

        given(virtualAccountService.getAccountInfo(accountId))
                .willReturn(response);

        // When
        mockMvc.perform(
                        get("/api/account/{accountId}", accountId)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                )

                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("계좌 내역 조회에 성공했습니다."))
                .andExpect(jsonPath("$.data.accountId").value(accountId));
    }

    @Test
    @DisplayName("[조회 200] OWNER 계정으로 계좌 조회 시")
    void getAccountTransactionsTest2() throws Exception {
        // Given
        int accountId = 100;
        AccountResponse response = new AccountResponse(
                0,
                accountId,
                BigDecimal.TEN,
                BigDecimal.ONE,
                LocalDateTime.now()
        );

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

    @Test
    @DisplayName("[조회 403] notOwnerToken 계정으로 OWNER 계좌 조회 시")
    void getAccountTransactionsTest4() throws Exception {
        // Given
        int accountId = 100;
        AccountResponse response = new AccountResponse(
                0,
                accountId,
                BigDecimal.TEN,
                BigDecimal.ONE,
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
}