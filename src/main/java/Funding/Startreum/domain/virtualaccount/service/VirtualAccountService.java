package Funding.Startreum.domain.virtualaccount.service;


import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import Funding.Startreum.domain.virtualaccount.dto.VirtualAccountDtos;
import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VirtualAccountService {

    private final VirtualAccountRepository repository;
    private final TransactionRepository transactionRepository;
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
