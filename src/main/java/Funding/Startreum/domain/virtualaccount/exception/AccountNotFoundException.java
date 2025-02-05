package Funding.Startreum.domain.virtualaccount.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(int accountId) {
        super("해당 계좌를 찾을 수 없습니다 : " + accountId);
    }

}
