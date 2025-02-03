package Funding.Startreum.domain.reward.repository;

import Funding.Startreum.domain.reward.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Integer> {
    List<Reward> findByProject_ProjectId(Integer projectId);
}
