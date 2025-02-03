package Funding.Startreum.domain.project.service;

import Funding.Startreum.common.util.JwtUtil;
import Funding.Startreum.domain.project.Project;
import Funding.Startreum.domain.project.repository.ProjectRepository;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public void deleteProject(Integer projectId, String token) {
        // "Bearer " 문자열 제거
        String email = jwtUtil.getEmailFromToken(token.replace("Bearer ", ""));
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."));
        Project findProject = projectRepository.findById(projectId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
        "해당 프로젝트를 찾을 수 없습니다."));
        if (!findProject.getCreator().getUserId().equals(user.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 작업을 수행할 권한이 없습니다."); //로그인한 유저와 프로젝트 유저 다를 시 403 에러 발생
        }

        // 프로젝트와 연관된 엔티티 삭제 (Cascade 설정이 되어 있으면 자동 삭제됨)
        projectRepository.delete(findProject);
    }
}
