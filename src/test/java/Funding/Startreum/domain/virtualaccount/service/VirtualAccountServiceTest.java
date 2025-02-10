package Funding.Startreum.domain.virtualaccount.service;

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
import Funding.Startreum.domain.virtualaccount.dto.response.AccountResponse;
import Funding.Startreum.domain.virtualaccount.entity.VirtualAccount;
import Funding.Startreum.domain.virtualaccount.repository.VirtualAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
class VirtualAccountServiceTest {

    @Mock
    private VirtualAccountRepository virtualAccountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private FundingRepository fundingRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private VirtualAccountService virtualAccountService;

    @Nested
    @DisplayName("findByName() 관련 테스트")
    class FindByNameTests {

        @Test
        @DisplayName("유저를 찾을 수 없는 경우")
        void whenUserNotFound_thenReturnDtosWithFalse() {
            String name = "nonexistent";
            when(userRepository.findByName(name)).thenReturn(Optional.empty());

            VirtualAccountDtos dtos = virtualAccountService.findByName(name);

            assertFalse(dtos.isAccountExists(), "User가 없으면 isAccountExists는 false여야 합니다.");
        }

        @Test
        @DisplayName("계좌를 찾을 수 없는 경우")
        void whenUserFoundButAccountNotFound_thenReturnDtosWithFalse() {
            String name = "user";
            User user = new User();
            user.setUserId(1);
            when(userRepository.findByName(name)).thenReturn(Optional.of(user));
            when(virtualAccountRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.empty());

            VirtualAccountDtos dtos = virtualAccountService.findByName(name);

            assertFalse(dtos.isAccountExists(), "계좌가 없으면 isAccountExists는 false여야 합니다.");
        }

        @Test
        @DisplayName("계좌가 올바르게 존재 할 경우")
        void whenUserAndAccountFound_thenReturnDtosFromEntity() {
            String name = "user";
            User user = new User();
            user.setUserId(1);
            when(userRepository.findByName(name)).thenReturn(Optional.of(user));

            VirtualAccount account = new VirtualAccount();
            account.setUser(user);
            account.setBalance(BigDecimal.valueOf(100));
            account.setCreatedAt(LocalDateTime.now());
            account.setUpdatedAt(LocalDateTime.now());
            when(virtualAccountRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(account));

            VirtualAccountDtos dtos = virtualAccountService.findByName(name);

            assertTrue(dtos.isAccountExists(), "계좌가 존재하면 isAccountExists는 true여야 합니다.");
            assertEquals(BigDecimal.valueOf(100), dtos.getBalance(), "계좌 잔액이 매핑되어야 합니다.");
        }

    }

    @Nested
    @DisplayName("createAccount() 관련 테스트")
    class CreateAccountTests {

        @Test
        @DisplayName("유저를 찾을 수 없는 경우")
        void whenUserNotFound_thenThrowException() {
            String name = "nonexistent";
            when(userRepository.findByName(name)).thenReturn(Optional.empty());

            Exception exception = assertThrows(IllegalArgumentException.class, () ->
                    virtualAccountService.createAccount(name)
            );
            assertTrue(exception.getMessage().contains("사용자를 찾을 수 없습니다"), "적절한 에러 메시지가 포함되어야 합니다.");

            verify(userRepository, times(1)).findByName(name);
        }

        @Test
        @DisplayName("계좌가 이미 존재할 경우")
        void whenAccountAlreadyExists_thenThrowException() {
            String name = "user";
            User user = new User();
            user.setUserId(1);
            when(userRepository.findByName(name)).thenReturn(Optional.of(user));

            VirtualAccount existingAccount = new VirtualAccount();
            existingAccount.setUser(user);
            when(virtualAccountRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.of(existingAccount));

            Exception exception = assertThrows(IllegalStateException.class, () ->
                    virtualAccountService.createAccount(name)
            );
            assertEquals("이미 계좌가 존재합니다.", exception.getMessage());
        }

        @Test
        @DisplayName("계좌를 성공적으로 생성했을 경우")
        void whenValid_thenCreateAccount() {
            String name = "user";
            User user = new User();
            user.setUserId(1);
            when(userRepository.findByName(name)).thenReturn(Optional.of(user));
            when(virtualAccountRepository.findByUser_UserId(user.getUserId())).thenReturn(Optional.empty());

            when(virtualAccountRepository.save(Mockito.<VirtualAccount>any()))
                    .thenAnswer(invocation -> {
                        VirtualAccount account = invocation.getArgument(0);
                        account.setUser(user);
                        return account;
                    });

            VirtualAccountDtos dtos = virtualAccountService.createAccount(name);

            assertTrue(dtos.isAccountExists(), "계좌 생성 시 success flag는 true여야 합니다.");
            assertEquals(BigDecimal.ZERO, dtos.getBalance(), "새 계좌의 초기 잔액은 0이어야 합니다.");

            verify(virtualAccountRepository, times(1)).save(Mockito.<VirtualAccount>any());
        }

    }

    @Nested
    @DisplayName("charge 계열 메서드 관련 테스트")
    class ChargeTests {

        @Test
        @DisplayName("계좌 ID 기반 충전")
        void testChargeByAccountId() {
            int accountId = 1;
            BigDecimal chargeAmount = BigDecimal.valueOf(30);

            VirtualAccount account = new VirtualAccount();
            account.setAccountId(accountId);
            account.setBalance(BigDecimal.valueOf(50));
            account.setUser(new User());
            when(virtualAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

            doAnswer(invocation -> {
                Transaction transaction = invocation.getArgument(0);
                transaction.setTransactionId(1); // 테스트용 모의 ID
                return transaction;
            }).when(transactionRepository).save(any(Transaction.class));

            AccountRequest request = new AccountRequest(chargeAmount);

            AccountPaymentResponse response = virtualAccountService.chargeByAccountId(accountId, request);

            // 30 + 50 = 80
            assertEquals(BigDecimal.valueOf(80), account.getBalance(), "충전 후 계좌 금액이 갱신 되어야만 합니다.");
            assertEquals(chargeAmount, response.chargeAmount(), "거래 금액이 응답에 포함되어야 합니다.");
        }

        @Test
        @DisplayName("username 기반 충전")
        void testChargeByUserName() {
            String username = "userCharge";
            BigDecimal chargeAmount = BigDecimal.valueOf(40);

            VirtualAccount account = new VirtualAccount();
            account.setAccountId(90);
            account.setBalance(BigDecimal.valueOf(60));
            User user = new User();
            user.setName(username);
            account.setUser(user);
            when(virtualAccountRepository.findByUser_Name(username)).thenReturn(Optional.of(account));

            doAnswer(invocation -> {
                Transaction transaction = invocation.getArgument(0);
                transaction.setTransactionId(1); // 테스트용 모의 ID
                return transaction;
            }).when(transactionRepository).save(any(Transaction.class));

            AccountRequest request = new AccountRequest(chargeAmount);
            AccountPaymentResponse response = virtualAccountService.chargeByUsername(username, request);

            // 60 + 40 = 100
            assertEquals(BigDecimal.valueOf(100), account.getBalance(), "충전 후 계좌 금액이 갱신 되어야만 합니다.");
            assertEquals(chargeAmount, response.chargeAmount(), "거래 금액이 응답에 포함되어야 합니다.");
        }
    }

    @Nested
    @DisplayName("payment() 관련 테스트")
    class PaymentTests {

        @Test
        @DisplayName("계좌 ID 기반 결제 성공")
        void testPaymentByAccountId_success() {
            // 테스트 시나리오 설정
            int payerAccountId = 10;
            int projectId = 1;
            String username = "payer";
            BigDecimal paymentAmount = BigDecimal.valueOf(50);

            // 프로젝트 객체 (결제 대상)
            Project project = new Project();
            project.setProjectId(projectId);
            project.setCurrentFunding(BigDecimal.ZERO);

            // 결제자 계좌 (충전 전 잔액: 200)
            VirtualAccount payerAccount = new VirtualAccount();
            payerAccount.setAccountId(payerAccountId);
            payerAccount.setBalance(BigDecimal.valueOf(200));
            payerAccount.setUser(new User());

            // 프로젝트 수혜자 계좌 (충전 전 잔액: 100)
            VirtualAccount projectAccount = new VirtualAccount();
            projectAccount.setAccountId(20);
            projectAccount.setBalance(BigDecimal.valueOf(100));
            projectAccount.setUser(new User());

            // Mocking repository 및 서비스 호출
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(virtualAccountRepository.findById(payerAccountId)).thenReturn(Optional.of(payerAccount));
            when(virtualAccountRepository.findBeneficiaryAccountByProjectId(projectId)).thenReturn(Optional.of(projectAccount));

            // sponsor 정보 (결제자)
            User sponsor = new User();
            sponsor.setUserId(2);
            sponsor.setName(username);
            when(userService.getUserByName(username)).thenReturn(sponsor);

            // transactionRepository.save 모의 (거래 ID 설정)
            doAnswer(invocation -> {
                Transaction tx = invocation.getArgument(0);
                tx.setTransactionId(100);
                return tx;
            }).when(transactionRepository).save(any(Transaction.class));

            // 결제 요청 객체 (projectId와 paymentAmount 포함)
            AccountPaymentRequest request = new AccountPaymentRequest(projectId, paymentAmount);

            // 결제 메서드 호출
            AccountPaymentResponse response = virtualAccountService.payment(payerAccountId, request, username);

            // 검증: 결제자 잔액 200 - 50 = 150, 프로젝트 계좌 잔액 100 + 50 = 150
            assertEquals(BigDecimal.valueOf(150), payerAccount.getBalance(), "결제 후 결제자 계좌 잔액이 갱신되어야 합니다.");
            assertEquals(BigDecimal.valueOf(150), projectAccount.getBalance(), "결제 후 프로젝트 계좌 잔액이 갱신되어야 합니다.");
            assertEquals(BigDecimal.valueOf(50), project.getCurrentFunding(), "프로젝트의 currentFunding이 갱신되어야 합니다.");

            // 응답 검증
            assertEquals(100, response.transactionId(), "거래 ID가 매핑되어야 합니다.");
            assertEquals(paymentAmount, response.chargeAmount(), "결제 금액이 응답에 포함되어야 합니다.");
        }

        @Test
        @DisplayName("username 기반 결제 성공")
        void testPaymentByUsername_success() {
            int projectId = 1;
            String username = "payer";
            BigDecimal paymentAmount = BigDecimal.valueOf(80);

            // 프로젝트 객체
            Project project = new Project();
            project.setProjectId(projectId);
            project.setCurrentFunding(BigDecimal.ZERO);

            // 결제자 계좌 (username 기반 조회, 초기 잔액: 300)
            VirtualAccount payerAccount = new VirtualAccount();
            payerAccount.setAccountId(30);
            payerAccount.setBalance(BigDecimal.valueOf(300));
            User payerUser = new User();
            payerUser.setName(username);
            payerAccount.setUser(payerUser);

            // 프로젝트 수혜자 계좌 (초기 잔액: 50)
            VirtualAccount projectAccount = new VirtualAccount();
            projectAccount.setAccountId(40);
            projectAccount.setBalance(BigDecimal.valueOf(50));
            projectAccount.setUser(new User());

            // Mocking: username으로 결제자 계좌 조회
            when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
            when(virtualAccountRepository.findByUser_Name(username)).thenReturn(Optional.of(payerAccount));
            when(virtualAccountRepository.findBeneficiaryAccountByProjectId(projectId)).thenReturn(Optional.of(projectAccount));

            // 후원자 정보
            User sponsor = new User();
            sponsor.setUserId(3);
            sponsor.setName(username);
            when(userService.getUserByName(username)).thenReturn(sponsor);

            // transactionRepository.save 모의
            doAnswer(invocation -> {
                Transaction tx = invocation.getArgument(0);
                tx.setTransactionId(101);
                return tx;
            }).when(transactionRepository).save(any(Transaction.class));

            AccountPaymentRequest request = new AccountPaymentRequest(projectId, paymentAmount);

            AccountPaymentResponse response = virtualAccountService.payment(request, username);

            // 검증: 결제자 잔액 300 - 80 = 220, 프로젝트 계좌 잔액 50 + 80 = 130, 프로젝트 currentFunding = 80
            assertEquals(BigDecimal.valueOf(220), payerAccount.getBalance(), "결제 후 결제자 계좌 잔액이 갱신되어야 합니다.");
            assertEquals(BigDecimal.valueOf(130), projectAccount.getBalance(), "결제 후 프로젝트 계좌 잔액이 갱신되어야 합니다.");
            assertEquals(BigDecimal.valueOf(80), project.getCurrentFunding(), "프로젝트 currentFunding이 갱신되어야 합니다.");

            // 응답 검증
            assertEquals(101, response.transactionId(), "거래 ID가 매핑되어야 합니다.");
            assertEquals(paymentAmount, response.chargeAmount(), "결제 금액이 응답에 포함되어야 합니다.");
        }
    }

    @Nested
    @DisplayName("getAccountInfo() 관련 테스트")
    class GetAccountInfoTests {

        @Test
        @DisplayName("계좌 ID 기반 계좌 정보 조회")
        void testGetAccountInfoByAccountId() {
            int accountId = 70;
            VirtualAccount account = new VirtualAccount();
            account.setAccountId(accountId);
            account.setBalance(BigDecimal.valueOf(500));
            when(virtualAccountRepository.findById(accountId)).thenReturn(Optional.of(account));

            AccountResponse response = virtualAccountService.getAccountInfo(accountId);

            assertNotNull(response, "계좌 정보 응답은 null이 아니어야 합니다.");
            assertEquals(accountId, response.accountId(), "응답 계좌 ID가 일치해야 합니다.");
        }

        @Test
        @DisplayName("username 기반 계좌 정보 조회")
        void testGetAccountInfoByUsername() {
            String username = "userInfo";
            VirtualAccount account = new VirtualAccount();
            account.setAccountId(80);
            account.setBalance(BigDecimal.valueOf(750));
            User user = new User();
            user.setName(username);
            account.setUser(user);
            when(virtualAccountRepository.findByUser_Name(username)).thenReturn(Optional.of(account));

            AccountResponse response = virtualAccountService.getAccountInfo(username);

            assertNotNull(response, "계좌 정보 응답은 null이 아니어야 합니다.");
            assertEquals(80, response.accountId(), "응답 계좌 ID가 일치해야 합니다.");
        }
    }


}
