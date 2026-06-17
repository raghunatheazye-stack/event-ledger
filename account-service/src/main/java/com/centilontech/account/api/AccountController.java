package com.centilontech.account.api;

import com.centilontech.account.api.AccountDtos.*;
import com.centilontech.account.service.AccountLedgerService;
import jakarta.validation.Valid;
import org.slf4j.MDC;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountLedgerService service;
    public AccountController(AccountLedgerService service){this.service=service;}
    @PostMapping("/{accountId}/transactions")
    ResponseEntity<TransactionResponse> apply(@PathVariable String accountId, @Valid @RequestBody TransactionRequest request,
        @RequestHeader(value="X-Trace-Id", required=false) String traceId) {
        var result=service.apply(accountId, request, traceId==null?MDC.get("traceId"):traceId);
        return ResponseEntity.status(result.duplicate()?HttpStatus.OK:HttpStatus.CREATED).body(result);
    }
    @GetMapping("/{accountId}/balance") BalanceResponse balance(@PathVariable String accountId){return service.balance(accountId);}
    @GetMapping("/{accountId}") AccountResponse account(@PathVariable String accountId){return service.account(accountId);}
}
