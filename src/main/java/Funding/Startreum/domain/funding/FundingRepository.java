package Funding.Startreum.domain.funding;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FundingRepository extends JpaRepository<Funding, Integer> {
    Optional<Funding> findByFundingId(Integer fundingId);
}
