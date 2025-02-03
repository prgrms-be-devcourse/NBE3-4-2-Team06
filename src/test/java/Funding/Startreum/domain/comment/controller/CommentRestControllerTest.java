package Funding.Startreum.domain.comment.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class CommentRestControllerTest {

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
    @DisplayName("Comment 생성 성공")
    void createComment_ValidRequest_ReturnsCreated() throws Exception {
        int projectId = 10;

        ResultActions result = mockMvc.perform(
                post("/api/comment/" + projectId)
                        .header("Authorization", "Bearer " + token)
                        .content("""
                            {
                                "content": "댓글 내용입니다."
                            }
                            """) // ✅ 마지막 쉼표 제거
                        .contentType(MediaType.APPLICATION_JSON)
        );

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("댓글 생성에 성공했습니다."));
    }

}