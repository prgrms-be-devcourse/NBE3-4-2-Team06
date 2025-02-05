package Funding.Startreum.domain.virtualaccount.repository;

import Funding.Startreum.domain.reward.Reward;
import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Integer> {
    Optional<VirtualAccount> findByUser_UserId(Integer userId);  // userId를 사용하여 VirtualAccount 찾기

    @Query("SELECT va FROM VirtualAccount va " +
            "JOIN va.user u " +
            "JOIN u.projects p " +
            "WHERE p.projectId = :projectId")
    Optional<VirtualAccount> findBeneficiaryAccountByProjectId(@Param("projectId") Integer projectId);
}
    
