package Funding.Startreum.domain.virtualaccount.service;

import Funding.Startreum.domain.transaction.entity.Transaction;
import Funding.Startreum.domain.transaction.repository.TransactionRepository;
import Funding.Startreum.domain.users.UserRepository;
import Funding.Startreum.domain.virtualaccount.dto.request.AccountRequest;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountResponse;
import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;
import Funding.Startreum.domain.virtualaccount.exception.AccountNotFoundException;
import Funding.Startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VirtualAccountService {

    private final VirtualAccountRepository repository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    // 계좌 충전
    @Transactional
    public AccountResponse charge(int accountId, AccountRequest request) {

        VirtualAccount account = repository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // 잔액 업데이트
        account.setBalance(account.getBalance().add(request.chargeAmount()));
        repository.save(account);

        // 거래 내역 생성
        Transaction transaction = new Transaction();
        transaction.setFunding(null); // 충전은 펀딩과 연관 없음
        transaction.setAdmin(userRepository.findByName("Admin").orElse(null));
        transaction.setSenderAccount(account);
        transaction.setReceiverAccount(account);
        transaction.setAmount(request.chargeAmount());
        transaction.setType(Transaction.TransactionType.REMITTANCE);
        transaction.setTransactionDate(LocalDateTime.now());

        transactionRepository.save(transaction);

        return mapToDto(account, transaction, request.chargeAmount());
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccountInfo(int accountId) {
        VirtualAccount account = repository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        return mapToDto(account);
    }

    private AccountResponse mapToDto(VirtualAccount account) {
        return new AccountResponse(
                0,
                account.getAccountId(),
                BigDecimal.ZERO,
                account.getBalance(),
                account.getUpdatedAt()
        );
    }

    private AccountResponse mapToDto(VirtualAccount account, Transaction transaction, BigDecimal chargeAmount) {
        return new AccountResponse(
                transaction.getTransactionId(),
                account.getAccountId(),
                chargeAmount,
                account.getBalance(),
                transaction.getTransactionDate()
        );
    }

}
