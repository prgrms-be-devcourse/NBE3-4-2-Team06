package Funding.Startreum.domain.virtualaccount.controller;

import Funding.Startreum.common.util.ApiResponse;
import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
class VirtualAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;

    @BeforeEach
    void setUp() {
        token = jwtUtil.generateAccessToken("testSponsor", "sponsor@test.com", "SPONSOR");
        System.out.println(token);
    }

    @Test
    void chargeVirtualAccountTest() throws Exception {
    }

    @Test
    @DisplayName("GET /api/account/{accountId} - 계좌 조회 성공 테스트")
    void getAccountTransactionsTest() throws Exception {
        AccountResponse response = new AccountResponse(
                0,
                1,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                LocalDateTime.now()
        );
        ApiResponse apiResponse = ApiResponse.success("계좌 내역 조회에 성공했습니다.", response);


    }
}