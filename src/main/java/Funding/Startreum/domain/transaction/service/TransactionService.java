package Funding.Startreum.domain.transaction.service;

import Funding.Startreum.domain.funding.FundingRepository;
import Funding.Startreum.domain.project.entity.Project;
import Funding.Startreum.domain.project.repository.ProjectRepository;
import Funding.Startreum.domain.transaction.dto.RemittanceResponseDto;
import Funding.Startreum.domain.transaction.repository.TransactionRepository;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;
import Funding.Startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import Funding.Startreum.domain.funding.Funding;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final ProjectRepository projectRepository;
    private final VirtualAccountRepository virtualAccountRepository;
    private final UserRepository userRepository;

    @Transactional
    public RemittanceResponseDto processRemittance(Integer projectId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."));
        if (!user.getRole().equals(User.Role.ADMIN)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 유저는 관리자가 아닙니다.");
        }

        // 1. 프로젝트 조회 (펀딩 성공 여부 확인)
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 프로젝트를 찾을 수 없습니다."));

        if (!Project.Status.SUCCESS.equals(project.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "펀딩이 성공하지 않은 프로젝트입니다.");
        }

        List<Funding> fundings = project.getFundings();
        if (fundings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 프로젝트에 대한 후원 내역이 없습니다.");
        }

        // 2. 각 후원자의 가상 계좌에서 fundingBlock을 false로 변경
        List<RemittanceResponseDto.RemittanceDetail> remittanceDetails = fundings.stream().map(funding -> {
            VirtualAccount supporterAccount = virtualAccountRepository.findByUser(funding.getSponsor())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "후원자의 가상 계좌를 찾을 수 없습니다."));

            supporterAccount.setFundingBlock(false); // 송금 가능하도록 변경
            virtualAccountRepository.save(supporterAccount);

            return new RemittanceResponseDto.RemittanceDetail(
                    funding.getFundingId(),
                    user.getUserId(),
                    supporterAccount.getAccountId(),
                    project.getCreator().getVirtualAccount().getAccountId(), // 수혜자의 계좌 ID
                    funding.getAmount(),
                    "remittance",
                    LocalDateTime.now()
            );
        }).toList();


        return new RemittanceResponseDto("ok", 200, "송금 처리에 성공했습니다.", projectId, remittanceDetails);
    }
}

