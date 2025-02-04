package Funding.Startreum.domain.project.service;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.project.Project;
import Funding.Startreum.domain.project.dto.ProjectApprovalResponseDto;
import Funding.Startreum.domain.project.repository.ProjectRepository;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public ProjectApprovalResponseDto requestApprove(Integer projectId, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다."));

        if (!project.getCreator().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다.");
        }

        project.setIsApproved(Project.ApprovalStatus.AWAITING_APPROVAL);
        projectRepository.save(project);

        return new ProjectApprovalResponseDto(
                200,
                "AWAITING_APPROVAL",
                "승인 요청에 성공하였습니다.",
                new ProjectApprovalResponseDto.Data(
                        projectId,
                        LocalDateTime.now()
                )
        );



    }
}
