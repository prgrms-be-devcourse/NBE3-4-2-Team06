package Funding.Startreum.domain.virtualaccount.exception;

import Funding.Startreum.common.util.ApiResponse;
import Funding.Startreum.domain.transaction.entity.Transaction;
import Funding.Startreum.domain.virtualaccount.controller.VirtualAccountController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = VirtualAccountController.class)
public class VirtualAccountExceptionHandler {

    private static final Map<Class<? extends RuntimeException>, HttpStatus> STATUS_MAP = Map.of(
            AccountNotFoundException.class, HttpStatus.NOT_FOUND,
            NotEnoughBalanceException.class, HttpStatus.PAYMENT_REQUIRED,
            TransactionNotFoundException.class, HttpStatus.NOT_FOUND,
            FundingNotFoundException.class, HttpStatus.NOT_FOUND
    );

    @ExceptionHandler({
            AccountNotFoundException.class,
            NotEnoughBalanceException.class,
            TransactionNotFoundException.class,
            FundingNotFoundException.class,
    })
    public ResponseEntity<ApiResponse<Void>> handleException(RuntimeException e) {
        HttpStatus status = STATUS_MAP.getOrDefault(e.getClass(), HttpStatus.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(status).body(ApiResponse.error(e.getMessage()));
    }

}
