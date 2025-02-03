package Funding.Startreum.domain.project.controller;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.project.Project;
import Funding.Startreum.domain.project.dto.ProjectUpdateRequestDto;
import Funding.Startreum.domain.project.dto.ProjectUpdateResponseDto;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        user.setCreatedAt(LocalDateTime.now());  // createdAt 설정
        user.setUpdatedAt(LocalDateTime.now());  // updatedAt 설정
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
    @DisplayName("프로젝트 수정 성공 테스트")
    void testModifyProject() throws Exception {
        // ✅ 4. 수정 요청 DTO 생성
        ProjectUpdateRequestDto requestDto = new ProjectUpdateRequestDto(
                "수정된 프로젝트 제목",
                "수정된 프로젝트 설명",
                new BigDecimal(1500000),
                LocalDateTime.of(2025, 2, 10, 0, 0),
                LocalDateTime.of(2025, 3, 10, 0, 0)
        );

        // ✅ 5. ProjectUpdateResponseDto 모의 응답 설정
        ProjectUpdateResponseDto responseDto = new ProjectUpdateResponseDto(
                projectId,
                requestDto.title(),
                requestDto.description(),
                requestDto.fundingGoal(),
                requestDto.startDate(),
                requestDto.endDate(),
                LocalDateTime.now() // 수정된 시간
        );

        BDDMockito.given(projectService.modifyProject(any(Integer.class), any(ProjectUpdateRequestDto.class), any(String.class)))
                .willReturn(responseDto);

        // ✅ 6. PUT 요청 수행
        ResultActions result = mockMvc.perform(put("/api/beneficiary/modify/" + projectId)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "수정된 프로젝트 제목",
                          "description": "수정된 프로젝트 설명",
                          "fundingGoal": 1500000,
                          "startDate": "2025-02-10T00:00:00",
                          "endDate": "2025-03-10T00:00:00"
                        }
                        """));

        // ✅ 7. 응답 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("프로젝트 수정에 성공하였습니다."))
                .andExpect(jsonPath("$.data.projectId").value(projectId))
                .andExpect(jsonPath("$.data.title").value("수정된 프로젝트 제목"))
                .andExpect(jsonPath("$.data.description").value("수정된 프로젝트 설명"))
                .andExpect(jsonPath("$.data.fundingGoal").value(1500000))
                .andExpect(jsonPath("$.data.startDate").value("2025-02-10T00:00:00"))
                .andExpect(jsonPath("$.data.endDate").value("2025-03-10T00:00:00"));
    }
}
