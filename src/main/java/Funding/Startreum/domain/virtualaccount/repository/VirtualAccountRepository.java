package Funding.Startreum.domain.virtualaccount.repository;

import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Integer> {
    Optional<VirtualAccount> findByUser_UserId(Integer userId);  // userId를 사용하여 VirtualAccount 찾기
}
    
