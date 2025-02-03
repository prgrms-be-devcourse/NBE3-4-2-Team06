package Funding.Startreum.domain.project.controller;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.project.Project;
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

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
        user.setPassword("password123"); // 비밀번호 암호화는 생략 (테스트 환경)
        user.setRole(User.Role.BENEFICIARY);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        // ✅ 2. JWT 토큰 생성
        token = jwtUtil.generateAccessToken("testUser", "test@test.com", "BENEFICIARY");
        System.out.println("token: " + token);

        // ✅ 3. 테스트용 프로젝트 생성
        Project project = new Project();
        project.setTitle("삭제할 프로젝트");
        project.setDescription("이 프로젝트는 삭제될 예정입니다.");
        project.setFundingGoal(new BigDecimal(500000));
        project.setStartDate(LocalDateTime.of(2025, 2, 1, 0, 0));
        project.setEndDate(LocalDateTime.of(2025, 3, 1, 0, 0));
        project.setCreator(user);
        projectRepository.save(project);

        projectId = project.getProjectId();
    }

    @Test
    @DisplayName("프로젝트 삭제 성공 테스트")
    void testDeleteProject() throws Exception {
        // ✅ 4. projectService.deleteProject 모의 응답 설정
        BDDMockito.doNothing().when(projectService).deleteProject(any(Integer.class), any(String.class));

        // ✅ 5. DELETE 요청 수행
        ResultActions result = mockMvc.perform(delete("/api/beneficiary/delete/" + projectId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));

        // ✅ 6. 응답 검증: HTTP 204 상태 코드 및 응답 본문 없음
        result.andExpect(status().isNoContent());  // 204 상태 코드
    }
}
