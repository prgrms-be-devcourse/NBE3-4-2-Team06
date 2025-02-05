package Funding.Startreum.domain.project.service;

import Funding.Startreum.domain.project.dto.ProjectCreateRequestDto;
import Funding.Startreum.domain.project.dto.ProjectCreateResponseDto;
import Funding.Startreum.domain.project.entity.Project;
import Funding.Startreum.domain.project.repository.ProjectRepository;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.ResponseCache;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectCreateResponseDto createProject(ProjectCreateRequestDto projectCreateRequestDto, String userId) {

        //사용자 검증
        User user = userRepository.findByEmail(userId).orElseThrow(() ->
        new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다"));    //사용자를 찾을 수 없을 시 401 에러

        //프로젝트 생성
        Project project = new Project();
        project.setCreator(user);
        project.setTitle(projectCreateRequestDto.title());
        project.setBannerUrl(projectCreateRequestDto.bannerUrl());
        project.setDescription(projectCreateRequestDto.description());
        project.setFundingGoal(projectCreateRequestDto.fundingGoal());
        project.setCurrentFunding(BigDecimal.ZERO);
        project.setStartDate(projectCreateRequestDto.startDate());
        project.setEndDate(projectCreateRequestDto.endDate());
        project.setStatus(Project.Status.ONGOING);
        project.setIsApproved(Project.ApprovalStatus.AWAITING_APPROVAL);
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        projectRepository.save(project);

        return new ProjectCreateResponseDto(project.getProjectId(), project.getTitle(), project.getCreatedAt());
    }
}
