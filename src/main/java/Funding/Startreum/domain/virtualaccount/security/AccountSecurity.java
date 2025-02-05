package Funding.Startreum.domain.virtualaccount.security;

import Funding.Startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountSecurity {

    private final VirtualAccountRepository repository;

    public boolean isAccountOwner(UserDetails userDetails, int accountId) {
        System.out.println("isAccountOwner 호출, userDetails: " + userDetails + ", accountId: " + accountId);

        // 계좌의 소유자(username)와 현재 로그인한 사용자의 username을 비교
        repository.findById(accountId)
                .map(account -> {
                    boolean isOwner = account.getUser().getName().equals(userDetails.getUsername());
                    if (!isOwner) {
                        throw new AccessDeniedException("해당 작업을 수행할 권한이 없습니다.");
                    }
                    return true; // 소유자가 맞으면 true
                });

        return true;
    }
}