package Funding.Startreum.domain.sponsor;

import Funding.Startreum.domain.funding.FundingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import Funding.Startreum.domain.funding.Funding;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SponsorService {

    private final FundingRepository fundingRepository;

    @Transactional
    public SponListResponse getFundingList(String username, Pageable pageable) {
        Page<Funding> fundingPage = fundingRepository.findBySponsorEmail(username, pageable);

        var fundings = fundingPage.getContent().stream()
                .map(funding -> new SponListResponse.Funding(
                    funding.getFundingId(),
                    funding.getProject().getProjectId(),
                    funding.getProject().getTitle(),
                    funding.getReward().getRewardId(),
                    funding.getAmount().doubleValue(),
                    funding.getProject().getCreatedAt()
                ))
                .toList();
        var pagination = new SponListResponse.Pagination(
                (int) fundingPage.getTotalElements(),
                pageable.getPageNumber() + 1,
                pageable.getPageSize()
        );

        return SponListResponse.success(fundings, pagination);
    }
}
