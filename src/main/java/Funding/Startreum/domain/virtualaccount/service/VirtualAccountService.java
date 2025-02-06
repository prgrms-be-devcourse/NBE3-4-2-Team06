package Funding.Startreum.domain.virtualaccount.service;


import Funding.Startreum.domain.funding.Funding;
import Funding.Startreum.domain.funding.FundingRepository;
import Funding.Startreum.domain.project.Project;
import Funding.Startreum.domain.project.ProjectRepository;
import Funding.Startreum.domain.reward.RewardRepository;
import Funding.Startreum.domain.transaction.entity.Transaction;
import Funding.Startreum.domain.transaction.repository.TransactionRepository;
import Funding.Startreum.domain.users.User;
import Funding.Startreum.domain.users.UserRepository;
import Funding.Startreum.domain.users.UserService;
import Funding.Startreum.domain.virtualaccount.dto.VirtualAccountDtos;
import Funding.Startreum.domain.virtualaccount.dto.request.AccountPaymentRequest;
import Funding.Startreum.domain.virtualaccount.dto.request.AccountRequest;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountPaymentResponse;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountResponse;
import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;
import Funding.Startreum.domain.virtualaccount.exception.AccountNotFoundException;
import Funding.Startreum.domain.virtualaccount.exception.NotEnoughBalanceException;
import Funding.Startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static Funding.Startreum.domain.transaction.entity.Transaction.TransactionType.REMITTANCE;
import static Funding.Startreum.domain.virtualaccount.dto.response.AccountPaymentResponse.mapToAccountResponse;
import static Funding.Startreum.domain.virtualaccount.dto.response.AccountResponse.FromOwnVirtualAccount;

@Service
@RequiredArgsConstructor
public class VirtualAccountService {

    private final VirtualAccountRepository virtualAccountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final RewardRepository rewardRepository;
    private final FundingRepository fundingRepository;

    private final UserService userService;


    /**
     * 사용자의 계좌 정보를 가져와 DTO로 반환
     */
    public VirtualAccountDtos findByName(String name) {
        User user = userRepository.findByName(name).orElse(null);
        if (user == null) {
            return new VirtualAccountDtos(false); // 계좌 없음 응답
        }

        VirtualAccount account = virtualAccountRepository.findByUser_UserId(user.getUserId()).orElse(null);
        return (account != null) ? VirtualAccountDtos.fromEntity(account) : new VirtualAccountDtos(false);
    }

    /**
     * 계좌 생성
     */
    public VirtualAccountDtos createAccount(String name) {
        User user = userRepository.findByName(name).orElseThrow(() ->
                new IllegalArgumentException("사용자를 찾을 수 없습니다: " + name));

        // 이미 계좌가 있는지 확인
        if (virtualAccountRepository.findByUser_UserId(user.getUserId()).isPresent()) {
            throw new IllegalStateException("이미 계좌가 존재합니다.");
        }

        VirtualAccount newAccount = new VirtualAccount();
        newAccount.setUser(user);
        newAccount.setBalance(BigDecimal.ZERO); // 초기 잔액 0원
        newAccount.setFundingBlock(false); // 기본적으로 펀딩 차단 없음
        newAccount.setCreatedAt(LocalDateTime.now());
        newAccount.setUpdatedAt(LocalDateTime.now());

        virtualAccountRepository.save(newAccount);
        return VirtualAccountDtos.fromEntity(newAccount);
    }

    /**
     * 계좌 잔액 충전
     *
     * @param accountId 조회할 계좌 ID
     * @param request   잔액 정보가 담겨진 DTO
     * @return AccountPaymentResponse DTO
     */
    @Transactional
    public AccountPaymentResponse charge(int accountId, AccountRequest request) {
        VirtualAccount account = getAccount(accountId);

        // 결제 전 금액
        BigDecimal beforeMoney = account.getBalance();

        // 잔액 업데이트
        account.setBalance(account.getBalance().add(request.amount()));
        virtualAccountRepository.save(account);

        // 거래 내역 생성
        Transaction transaction = createTransaction(null, account, account, request.amount(), REMITTANCE);
        transactionRepository.save(transaction);

        return mapToAccountResponse(account, transaction, beforeMoney, request.amount(), account.getBalance());
    }

    /**
     * 계좌를 조회합니다.
     *
     * @param accountId 조회할 계좌 ID
     * @return 계좌
     */
    @Transactional(readOnly = true)
    public VirtualAccount getAccount(int accountId) {
        return virtualAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    /**
     * 계좌 잔액 조회
     *
     * @param accountId 조회할 계좌 ID
     * @return 계좌 응답 DTO
     */
    @Transactional(readOnly = true)
    public AccountResponse getAccountInfo(int accountId) {
        return FromOwnVirtualAccount(getAccount(accountId));
    }

    /**
     * 결제를 진행하는 로직입니다.
     *
     * @param accountId 결제할 사용자의 게좌 ID
     * @param request   projectId과 결제 금액이 담겨져 있는 DTO
     * @param username  결제할 사용자 이름
     * @return 결제한 계좌의 정보 DTO를 반환합니다.
     */
    @Transactional
    public AccountPaymentResponse payment(int accountId, AccountPaymentRequest request, String username) {
        // 1. 프로젝트 조회
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 프로젝트 입니다: " + request.projectId()));

        // 2. 계좌 조회
        VirtualAccount sendAccount = getAccount(accountId);
        VirtualAccount receiveAccount = virtualAccountRepository.findBeneficiaryAccountByProjectId(request.projectId())
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // 3. 결제 로직 - 계좌 잔액 업데이트
        BigDecimal beforeMoney = sendAccount.getBalance();
        BigDecimal paymentAmount = request.amount();

        processAccountPayment(beforeMoney, paymentAmount, sendAccount, receiveAccount);

        // 4. 펀딩 내역 저장
        Funding funding = createFunding(project, username, paymentAmount);

        // 5. 거래 내역 생성
        Transaction transaction = createTransaction(funding, sendAccount, receiveAccount, paymentAmount, REMITTANCE);
        transactionRepository.save(transaction);

        // 6. 응답 객체 생성 및 반환
        return mapToAccountResponse(sendAccount, transaction, beforeMoney, paymentAmount, sendAccount.getBalance());
    }

    /**
     * 결제 금액에 따른 계좌 잔액 업데이트를 진행합니다.
     *
     * @param beforeMoney    결제 전 금액
     * @param paymentAmount  결제 금액
     * @param sendAccount    송금 계좌
     * @param receiveAccount 수신 계좌
     * @throws RuntimeException ️ ⚠️ 결제전 금액이 결제 금액보다 부족할 경우, 예외 논의 필요
     */
    private void processAccountPayment(BigDecimal beforeMoney, BigDecimal paymentAmount, VirtualAccount sendAccount, VirtualAccount receiveAccount) {
        if (beforeMoney.compareTo(paymentAmount) < 0) {
            throw new NotEnoughBalanceException(beforeMoney);
        }
        sendAccount.setBalance(beforeMoney.subtract(paymentAmount));
        virtualAccountRepository.save(sendAccount);
        receiveAccount.setBalance(receiveAccount.getBalance().add(paymentAmount));
        virtualAccountRepository.save(receiveAccount);
    }

    /**
     * 펀딩 내역을 저장 후 반환합니다.
     *
     * @param project       펀딩할 프로젝트
     * @param username      펀딩한 유저
     * @param paymentAmount 펀딩 금액
     * @return 정보가 담긴 Funding 객체
     */
    private Funding createFunding(Project project, String username, BigDecimal paymentAmount) {
        User sponsor = userService.getUserByName(username);

        Funding funding = new Funding();
        funding.setProject(project);
        funding.setAmount(paymentAmount);
        funding.setFundedAt(LocalDateTime.now());
        funding.setSponsor(sponsor);

        // 리워드 할당: 결제 금액이 리워드 기준 이하인 경우,
        rewardRepository.findTopByProject_ProjectIdAndAmountLessThanEqualOrderByAmountDesc(project.getProjectId(), paymentAmount)
                .ifPresent(funding::setReward);

        fundingRepository.save(funding);
        return funding;
    }

    /**
     * 거래 내역 생성 메서드
     */
    private Transaction createTransaction(Funding funing, VirtualAccount sendAccount, VirtualAccount receiveAccount, BigDecimal amount, Transaction.TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setFunding(funing);
        transaction.setAdmin(userRepository.findByName("Admin").orElse(null));
        transaction.setSenderAccount(sendAccount);
        transaction.setReceiverAccount(receiveAccount);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTransactionDate(LocalDateTime.now());

        return transaction;
    }

}
