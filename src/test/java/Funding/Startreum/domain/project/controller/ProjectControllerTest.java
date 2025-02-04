package Funding.Startreum.domain.project.controller;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.project.Project;
import Funding.Startreum.domain.project.dto.ProjectApprovalResponseDto;
import Funding.Startreum.domain.project.repository.ProjectRepository;
import Funding.Startreum.domain.project.service.ProjectService;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @MockitoBean
    private ProjectService projectService;

    private String token;
    private Integer projectId;

    @BeforeEach
    void setUp() {
        // ✅ 1. 테스트용 사용자 생성
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password123");
        user.setRole(User.Role.BENEFICIARY);
        user.setCreatedAt(LocalDateTime.now());  // createdAt 설정
        user.setUpdatedAt(LocalDateTime.now());  // updatedAt 설정
        user.setName("testUser");
        userRepository.save(user);

        // ✅ 2. JWT 토큰 생성
        token = jwtUtil.generateAccessToken("testUser", "test@test.com", "BENEFICIARY");
        System.out.println("token: " + token);

        // ✅ 3. 테스트용 프로젝트 생성
        Project project = new Project();
        project.setTitle("기존 프로젝트 제목");
        project.setDescription("기존 프로젝트 설명");
        project.setFundingGoal(new BigDecimal(1000000));
        project.setStartDate(LocalDateTime.of(2025, 2, 1, 0, 0));
        project.setEndDate(LocalDateTime.of(2025, 3, 1, 0, 0));
        project.setCreator(user);
        projectRepository.save(project);
        System.out.println(project.getCreator().getUserId());

        projectId = project.getProjectId();
    }

    @Test
    @DisplayName("프로젝트 승인 요청 성공 테스트")
    void testRequestApprovalSuccess() throws Exception {
        // Mocking ProjectService의 응답
        ProjectApprovalResponseDto mockResponse = new ProjectApprovalResponseDto(
                200,
                "AWAITING_APPROVAL",
                "승인 요청에 성공하였습니다.",
                new ProjectApprovalResponseDto.Data(
                        1,  // 예시 projectId
                        LocalDateTime.now()
                )
        );

        // ProjectService의 requestApprove 메서드가 호출될 때 mockResponse 반환
        BDDMockito.given(projectService.requestApprove(anyInt(), anyString()))
                .willReturn(mockResponse);

        // MockMvc를 사용하여 POST 요청 수행
        ResultActions result = mockMvc.perform(post("/api/beneficiary/requestApprove/" + 1)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));

        // 응답 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.status").value("AWAITING_APPROVAL"))
                .andExpect(jsonPath("$.message").value("승인 요청에 성공하였습니다."))
                .andExpect(jsonPath("$.data.projectId").value(1))
                .andExpect(jsonPath("$.data.requestedAt").exists());
    }
}