package Funding.Startreum.domain.reward.service;

import Funding.Startreum.domain.project.Project;
import Funding.Startreum.domain.project.ProjectRepository;
import Funding.Startreum.domain.reward.dto.request.RewardRequest;
import Funding.Startreum.domain.reward.dto.request.RewardUpdateRequest;
import Funding.Startreum.domain.reward.dto.response.RewardResponse;
import Funding.Startreum.domain.reward.entity.Reward;
import Funding.Startreum.domain.reward.repository.RewardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RewardService {
    private final RewardRepository repository;
    private final ProjectRepository projectRepository;

    @Autowired
    public RewardService(RewardRepository repository, ProjectRepository projectRepository) {
        this.repository = repository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public RewardResponse createReward(RewardRequest request) {
        // 프로젝트 ID로 Project 엔티티 조회
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 프로젝트 ID입니다: " + request.projectId()));

        // Reward 엔티티 생성 및 설정
        Reward reward = new Reward();
        reward.setDescription(request.description());
        reward.setAmount(request.amount());
        reward.setProject(project);
        reward.setCreatedAt(LocalDateTime.now());
        reward.setUpdatedAt(LocalDateTime.now());

        repository.save(reward);

        // Reward 저장 및 반환
        return mapToDto(reward);
    }

    // project의 reward 검색
    @Transactional(readOnly = true)
    public List<RewardResponse> getRewardsByProjectId(int projectId) {

        // 프로젝트에 속한 리워드 조회
        List<Reward> rewards = repository.findByProject_ProjectId(projectId);

        // Reward 엔티티를 RewardResponse DTO로 변환
        return rewards.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RewardResponse updateReward(Integer rewardId, RewardUpdateRequest request) {

        Reward reward = repository.findById(rewardId)
                .orElseThrow(() -> new EntityNotFoundException("리워드를 찾을 수 없습니다 : " + rewardId));

        // 리워드 업데이트
        reward.setDescription(request.description());
        reward.setAmount(request.amount());
        reward.setUpdatedAt(LocalDateTime.now());

        // 업데이트된 리워드 저장
        repository.save(reward);

        // DTO로 변환하여 반환
        return mapToDto(reward);
    }

    @Transactional
    public void deleteReward(int rewardId) {
        Reward reward = repository.findById(rewardId)
                .orElseThrow(() -> new EntityNotFoundException("리워드를 찾을 수 없습니다 : " + rewardId));

        repository.delete(reward);
    }

    private RewardResponse mapToDto(Reward reward) {
        return new RewardResponse(
                reward.getRewardId(),
                reward.getProject().getProjectId(),
                reward.getDescription(),
                reward.getAmount(),
                reward.getCreatedAt(),
                reward.getUpdatedAt()
        );
    }

}
