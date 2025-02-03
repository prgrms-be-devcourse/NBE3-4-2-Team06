package Funding.Startreum.controller;

import Funding.Startreum.common.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** TODO : 현재 불완전한 코드임.
 * 프로젝트가 완료되면 BeforeEach에 project와 reward 자동 생성 및 삭제 로직 넣을 것
 */
@SpringBootTest
@AutoConfigureMockMvc
class RewardRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    private String token;

    @BeforeEach
    void setUp() {
        token = jwtUtil.generateAccessToken(
                "testUser",
                "test@example.com",
                "ADMIN"
        );
    }

    @Test
    @DisplayName("Reward 생성 성공")
    void createReward_ValidRequest_ReturnsCreated() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/reward")
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {
                                    "projectId": "10",
                                    "description": "이 리워드는 특별한 혜택을 제공합니다.",
                                    "amount": "100000"
                                }
                                """.stripIndent())
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        );

         String responseContent = result.andReturn().getResponse().getContentAsString();
         System.out.println("응답 내용: " + responseContent);

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("리워드 생성에 성공했습니다."));
    }

    @Test
    @DisplayName("Reward 생성 실패 - projectId 누락")
    void createReward_MissingProjectId_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(
                        post("/api/reward")
                                .header("Authorization", "Bearer " + token)
                                .content("""
                                        {
                                            "description": "이 리워드는 특별한 혜택을 제공합니다.",
                                            "amount": "100000"
                                        }
                                        """)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Reward 생성 실패 - description 누락")
    void createReward_MissingDesciption_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/reward")
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {
                                    "projectId": "100000",
                                    "amount": "100000"
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Reward 생성 실패 - amount 누락")
    void createReward_MissingAmount_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/reward")
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {
                                    "projectId": "100000",
                                    "description": "이 리워드는 특별한 혜택을 제공합니다."
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Reward 생성 실패 - amount == 0")
    void createReward_ZeroAmount_ReturnsBadRequest() throws Exception {
        ResultActions result = mockMvc.perform(
                post("/api/reward")
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {
                                    "projectId": "100000",
                                    "description": "이 리워드는 특별한 혜택을 제공합니다.",
                                    "amount": "0"
                                }
                                """)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Reward 검색 성공 - Reward가 있을 경우")
    void getReward_ReturnsOk () throws Exception {
        int projectId = 10;

        ResultActions result = mockMvc.perform(
                get("/api/reward/" + projectId)
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("리워드 조회에 성공했습니다."));
                // .andExpect(jsonPath("$.message").value("리워드가 존재하지 않습니다."));
    }

    @Test
    @DisplayName("Reward 검색 성공 - 프로젝트 없음")
    void getReward_NoProject_ReturnsOk() throws Exception {
        int projectId = 100000;

        ResultActions result = mockMvc.perform(
                get("/api/reward/" + projectId)
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("리워드가 존재하지 않습니다."));
    }


    @Test
    @DisplayName("리워드 수정 성공")
    void updateReward_ReturnsOk() throws Exception {
        int rewardId = 10;

        ResultActions result = mockMvc.perform(
                put("/api/reward/" + rewardId)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {
                                    "description": "수정된 리워드 입니다.",
                                    "amount": "1000"
                                }
                                """.stripIndent())
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        );

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("리워드 수정에 성공했습니다."));
    }

    @Test
    @DisplayName("리워드 수정 실패 - description 누락")
    void updateReward_MissingDescription_ReturnsBadRequest() throws Exception {
        int rewardId = 10;

        ResultActions result = mockMvc.perform(
                put("/api/reward/" + rewardId)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {
                                    "amount": "1000"
                                }
                                """.stripIndent())
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리워드 수정 실패 - amount 누락")
    void updateReward_MissingAmount_ReturnsBadRequest() throws Exception {
        int rewardId = 10;

        ResultActions result = mockMvc.perform(
                put("/api/reward/" + rewardId)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {
                                    "description": "수정된 리워드 입니다."                                    
                                }
                                """.stripIndent())
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리워드 수정 실패 - amount == 0")
    void updateReward_AmountZero_ReturnsBadRequest() throws Exception {
        int rewardId = 10;

        ResultActions result = mockMvc.perform(
                put("/api/reward/" + rewardId)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                                {
                                    "description": "수정된 리워드 입니다.",
                                    "amount": "0"
                                }
                                """.stripIndent())
                        .contentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8))
        ).andExpect(status().isBadRequest());
    }
//
//    @Test
//    void deleteReward() {
//    }
}