package Funding.Startreum.domain.virtualaccount.exception;

import Funding.Startreum.common.util.ApiResponse;
import Funding.Startreum.domain.virtualaccount.controller.VirtualAccountController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = VirtualAccountController.class)
public class VirtualAccountExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountNotFound(AccountNotFoundException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(NotEnoughBalanceException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotEnoughBalance(NotEnoughBalanceException e) {
        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(ApiResponse.error(e.getMessage()));
    }

}
