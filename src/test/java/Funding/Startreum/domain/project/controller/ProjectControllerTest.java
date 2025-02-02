package Funding.Startreum.domain.project.controller;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.project.dto.ProjectCreateRequestDto;
import Funding.Startreum.domain.project.dto.ProjectCreateResponseDto;
import Funding.Startreum.domain.project.service.ProjectService;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;  // UserRepository import 추가
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired  // UserRepository 주입
    private UserRepository userRepository;

    @MockitoBean
    private ProjectService projectService;

    private String token;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password123"); // 실제 애플리케이션에서는 비밀번호를 암호화해야 함
        user.setRole(User.Role.BENEFICIARY);  // 'BENEFICIARY' 역할로 설정
        user.setCreatedAt(LocalDateTime.now());  // createdAt 설정
        user.setUpdatedAt(LocalDateTime.now());  // updatedAt 설정
        userRepository.save(user);  // DB에 유저 저장

        // 테스트용 토큰 생성 (JwtUtil을 통해)
        token = jwtUtil.generateAccessToken("testUser", "test@test.com", "BENEFICIARY");  // 'BENEFICIARY' 역할로 설정
        System.out.println("token: " + token);
    }


    @Test
    @DisplayName("프로젝트 생성 테스트")
    void testCreateProject() throws Exception {
        // 프로젝트 생성 요청 DTO
        ProjectCreateRequestDto requestDto = new ProjectCreateRequestDto(
                "Test Project", // title
                "Description of test project", // description
                new BigDecimal(100000), // fundingGoal
                "https://example.com/banner.jpg", // bannerUrl
                LocalDateTime.now(), // startDate
                LocalDateTime.now().plusMonths(1) // endDate
        );

        // 프로젝트 생성 응답 DTO
        ProjectCreateResponseDto responseDto = new ProjectCreateResponseDto(1, "Test Project", LocalDateTime.now());

        // ProjectService의 createProject 메서드가 호출될 때 responseDto를 반환하도록 설정
        BDDMockito.given(projectService.createProject(any(ProjectCreateRequestDto.class), any(String.class)))
                .willReturn(responseDto);

        // 요청 보내기
        ResultActions result = mockMvc.perform(post("/api/create/projects")
                .header("Authorization", "Bearer " + token)  // Authorization 헤더에 Bearer 토큰 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Test Project\",\"description\":\"Description of test project\",\"fundingGoal\":100000,\"bannerUrl\":\"https://example.com/banner.jpg\",\"startDate\":\"2025-01-01T00:00:00\",\"endDate\":\"2025-02-01T00:00:00\"}"));

        // 응답 검증
        result.andExpect(status().isCreated()); // 응답 코드가 201 Created이어야 함
    }
}
