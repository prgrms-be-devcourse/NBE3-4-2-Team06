package Funding.Startreum.domain.virtualaccount.service;


import Funding.Startreum.domain.funding.Funding;
import Funding.Startreum.domain.funding.FundingRepository;
import Funding.Startreum.domain.project.entity.Project;
import Funding.Startreum.domain.project.repository.ProjectRepository;
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
import Funding.Startreum.domain.virtualaccount.dto.response.AccountRefundResponse;
import Funding.Startreum.domain.virtualaccount.dto.response.AccountResponse;
import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;
import Funding.Startreum.domain.virtualaccount.exception.AccountNotFoundException;
import Funding.Startreum.domain.virtualaccount.exception.FundingNotFoundException;
import Funding.Startreum.domain.virtualaccount.exception.NotEnoughBalanceException;
import Funding.Startreum.domain.virtualaccount.exception.TransactionNotFoundException;
import Funding.Startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static Funding.Startreum.domain.transaction.entity.Transaction.TransactionType.REFUND;
import static Funding.Startreum.domain.transaction.entity.Transaction.TransactionType.REMITTANCE;
import static Funding.Startreum.domain.virtualaccount.dto.response.AccountPaymentResponse.mapToAccountPaymentResponse;
import static Funding.Startreum.domain.virtualaccount.dto.response.AccountRefundResponse.mapToAccountRefundResponse;
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
     * 계좌 잔액을 충전합니다.
     *
     * @param accountId 조회할 계좌 ID
     * @param request   잔액 정보가 담겨진 DTO
     * @return 충전 후 갱신된 계좌 정보 DTO
     */
    @Transactional
    public AccountPaymentResponse charge(int accountId, AccountRequest request) {
        // 1. 계좌 조회
        VirtualAccount account = getAccount(accountId);

        // 2. 잔액 업데이트 로직
        BigDecimal beforeMoney = account.getBalance();
        account.setBalance(account.getBalance().add(request.amount()));
        virtualAccountRepository.save(account);

        // 3. 거래 내역 생성
        Transaction transaction = createTransaction(null, account, account, request.amount(), REMITTANCE);

        // 4. 응답 객체 생성 및 반환
        return mapToAccountPaymentResponse(account, transaction, beforeMoney, request.amount());
    }

    /**
     * 계좌를 조회합니다.
     *
     * @param accountId 조회할 계좌 ID
     * @return 조회한 계좌를 반환합니다.
     */
    @Transactional(readOnly = true)
    public VirtualAccount getAccount(int accountId) {
        return virtualAccountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    /**
     * 계좌를 조회합니다.
     *
     * @param accountId 조회할 계좌 ID
     * @return 조회한 계좌의 정보 DTO를 반환합니다.
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
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다. 프로젝트 ID: " + request.projectId()));

        // 2. 계좌 조회
        VirtualAccount payerAccount = getAccount(accountId);
        VirtualAccount projectAccount = virtualAccountRepository.findBeneficiaryAccountByProjectId(request.projectId())
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        // 3. 결제 로직 - 계좌 잔액 업데이트 (payerAccount에서 금액 차감, projectAccount에 금액 추가)
        BigDecimal payerBalanceBefore = payerAccount.getBalance();
        BigDecimal paymentAmount = request.amount();
        processAccountPayment(payerBalanceBefore, paymentAmount, payerAccount, projectAccount);

        // 4. 펀딩 및 거래 내역 저장
        Funding funding = createFunding(project, username, paymentAmount);
        Transaction transaction = createTransaction(funding, payerAccount, projectAccount, paymentAmount, REMITTANCE);

        // 5. 응답 객체 생성 및 반환 (결제 후 결제자 계좌 정보를 기준)
        return mapToAccountPaymentResponse(payerAccount, transaction, payerBalanceBefore, paymentAmount);
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
     * 환불을 진행하는 로직입니다.
     *
     * @param payerAccountId 환불 받을 사용자 계좌 ID (결제한 계좌)
     * @param transactionId  원 거래의 ID
     * @return 환불 완료 후 갱신된 계좌 정보 DTO
     */
    @Transactional
    public AccountRefundResponse refund(int payerAccountId, int transactionId) {
        // 1. 원 거래 내역 조회
        Transaction oldTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        // 2. 계좌 조회: 환불은 수혜자 계좌에서 결제자(환불 대상) 계좌로 자금 이동
        VirtualAccount payerAccount = getAccount(payerAccountId);
        VirtualAccount projectAccount = virtualAccountRepository.findReceiverAccountByTransactionId(transactionId)
                .orElseThrow(() -> new AccountNotFoundException(transactionId));

        // 3. 환불 로직 - 계좌 잔액 업데이트 (projectAccount에서 환불 금액 차감, payerAccount에 추가)
        BigDecimal beforeMoney = payerAccount.getBalance();
        BigDecimal refundAmount = oldTransaction.getAmount();
        processAccountPayment(beforeMoney, refundAmount, projectAccount, payerAccount);

        // 4. 펀딩 취소 및 환불 거래 내역 저장
        Funding funding = cancelFunding(oldTransaction.getFunding().getFundingId());
        Transaction newTransaction = createTransaction(funding, projectAccount, payerAccount, refundAmount, REFUND);

        // 5. 응답 객체 생성 및 반환 (환불 후 결제자 계좌 정보를 기준)
        return mapToAccountRefundResponse(payerAccount, newTransaction, transactionId, refundAmount, beforeMoney);
    }

    /**
     * 펀딩 내역을 취소합니다.
     *
     * @param fundingId 취소할 펀딩 ID
     * @return 취소된 Funding 객체
     */
    private Funding cancelFunding(Integer fundingId) {
        Funding funding = fundingRepository.findByFundingId(fundingId)
                .orElseThrow(() -> new FundingNotFoundException(fundingId));

        funding.setDeleted(true);
        fundingRepository.save(funding);

        return funding;
    }

    /**
     * 결제 또는 환불 시 계좌 잔액 업데이트를 진행합니다.
     *
     * @param sourceBalance 결제(또는 환불) 전 출금 계좌의 잔액
     * @param amount        거래 금액
     * @param sourceAccount 출금(또는 환불 출금) 계좌
     * @param targetAccount 입금(또는 환불 입금) 계좌
     * @throws RuntimeException 잔액이 부족할 경우 예외 발생
     */
    private void processAccountPayment(BigDecimal sourceBalance, BigDecimal amount, VirtualAccount sourceAccount, VirtualAccount targetAccount) {
        if (sourceBalance.compareTo(amount) < 0) {
            throw new NotEnoughBalanceException(sourceBalance);
        }
        sourceAccount.setBalance(sourceBalance.subtract(amount));
        virtualAccountRepository.save(sourceAccount);
        targetAccount.setBalance(targetAccount.getBalance().add(amount));
        virtualAccountRepository.save(targetAccount);
    }

    /**
     * 거래 내역 생성 메서드
     *
     * @param funding         관련 펀딩 내역
     * @param senderAccount   자금 출금 계좌 (결제 시에는 결제자, 환불 시에는 프로젝트 계좌)
     * @param receiverAccount 자금 입금 계좌 (결제 시에는 프로젝트 계좌, 환불 시에는 결제자 계좌)
     * @param amount          거래 금액
     * @param type            거래 유형 (REMITTANCE 또는 REFUND)
     * @return 생성된 Transaction 객체
     */
    private Transaction createTransaction(Funding funding, VirtualAccount senderAccount, VirtualAccount receiverAccount, BigDecimal amount, Transaction.TransactionType type) {
        Transaction transaction = new Transaction();
        transaction.setFunding(funding);
        transaction.setAdmin(userRepository.findByName("Admin").orElse(null));
        transaction.setSenderAccount(senderAccount);
        transaction.setReceiverAccount(receiverAccount);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setTransactionDate(LocalDateTime.now());

        transactionRepository.save(transaction);

        return transaction;
    }
}
