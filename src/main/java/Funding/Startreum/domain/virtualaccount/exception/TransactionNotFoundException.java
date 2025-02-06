package Funding.Startreum.domain.virtualaccount.exception;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(int transactionId) {
        super("해당 거래 내역을 찾을 수 없습니다 : " + transactionId);
    }

}
