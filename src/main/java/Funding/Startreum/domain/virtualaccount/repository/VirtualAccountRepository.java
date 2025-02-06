package Funding.Startreum.domain.virtualaccount.repository;

import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, Integer> {

    Optional<VirtualAccount> findByUser(User sponsor);
}
