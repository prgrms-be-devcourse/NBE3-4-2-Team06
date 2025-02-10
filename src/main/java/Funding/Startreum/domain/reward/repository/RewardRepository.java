package Funding.Startreum.domain.reward.repository;

<<<<<<< HEAD:src/main/java/Funding/Startreum/domain/reward/RewardRepository.java
=======

import Funding.Startreum.domain.reward.entity.Reward;
>>>>>>> a130cd991ceade7f88498a41015e63de5e9b417a:src/main/java/Funding/Startreum/domain/reward/repository/RewardRepository.java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Integer> {
}