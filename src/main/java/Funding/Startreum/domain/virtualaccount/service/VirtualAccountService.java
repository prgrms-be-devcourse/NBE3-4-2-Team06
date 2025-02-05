package Funding.Startreum.domain.virtualaccount.service;

import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import Funding.Startreum.domain.virtualaccount.dto.VirtualAccountDtos;
import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;
import Funding.Startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VirtualAccountService {

    private final VirtualAccountRepository repository;
    private final UserRepository userRepository;



    /**
     * 사용자의 계좌 정보를 가져와 DTO로 반환
     */
    public VirtualAccountDtos findByName(String name) {
        User user = userRepository.findByName(name).orElse(null);
        if (user == null) {
            return new VirtualAccountDtos(false); // 계좌 없음 응답
        }

        VirtualAccount account = repository.findByUser_UserId(user.getUserId()).orElse(null);
        return (account != null) ? VirtualAccountDtos.fromEntity(account) : new VirtualAccountDtos(false);
    }

    /**
     * 계좌 생성
     */
    public VirtualAccountDtos createAccount(String name) {
        User user = userRepository.findByName(name).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다: " + name));

        // 이미 계좌가 있는지 확인
        if (repository.findByUser_UserId(user.getUserId()).isPresent()) {
            throw new IllegalStateException("이미 계좌가 존재합니다.");
        }

        VirtualAccount newAccount = new VirtualAccount();
        newAccount.setUser(user);
        newAccount.setBalance(BigDecimal.ZERO); // 초기 잔액 0원
        newAccount.setFundingBlock(false); // 기본적으로 펀딩 차단 없음
        newAccount.setCreatedAt(LocalDateTime.now());
        newAccount.setUpdatedAt(LocalDateTime.now());

        repository.save(newAccount);
        return VirtualAccountDtos.fromEntity(newAccount);
    }

}
