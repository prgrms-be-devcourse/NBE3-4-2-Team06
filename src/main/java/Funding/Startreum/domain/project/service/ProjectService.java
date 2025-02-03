package Funding.Startreum.domain.project.service;


import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.project.Project;
import Funding.Startreum.domain.project.dto.ProjectUpdateRequestDto;
import Funding.Startreum.domain.project.repository.ProjectRepository;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Transactional
    public Project modifyProject(Integer projectId, ProjectUpdateRequestDto projectUpdateRequestDto, String token) {
        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다"));    //사용자를 찾을 수 없을 시 401 에러
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다."));    //프로젝트를 찾을 수 없을 시 404 에러

        if (!project.getCreator().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."); //로그인한 유저와 프로젝트 유저 다를 시 403 에러 발생
        }

        // 선택적으로 수정 (null이 아닌 값만 업데이트)
        if (projectUpdateRequestDto.title() != null) {
            project.setTitle(projectUpdateRequestDto.title());
        }
        if (projectUpdateRequestDto.description() != null) {
            project.setDescription(projectUpdateRequestDto.description());
        }
        if (projectUpdateRequestDto.fundingGoal() != null) {
            project.setFundingGoal(projectUpdateRequestDto.fundingGoal());
        }
        if (projectUpdateRequestDto.startDate() != null) {
            project.setStartDate(projectUpdateRequestDto.startDate());
        }
        if (projectUpdateRequestDto.endDate() != null) {
            project.setEndDate(projectUpdateRequestDto.endDate());
        }

        project.setUpdatedAt(LocalDateTime.now());

        return projectRepository.save(project);
    }

}
