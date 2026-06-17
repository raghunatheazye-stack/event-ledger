package com.centilontech.account.service;

import com.centilontech.account.api.AccountDtos.*;
import com.centilontech.account.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
public class AccountLedgerService {
    private final AccountTransactionRepository repository;
    public AccountLedgerService(AccountTransactionRepository repository){this.repository=repository;}

    @Transactional
    public TransactionResponse apply(String accountId, TransactionRequest request, String traceId) {
        var existing=repository.findByEventId(request.eventId());
        if(existing.isPresent()) return response(existing.get(), true);
        var saved=repository.saveAndFlush(new AccountTransaction(request.eventId(), accountId, request.type(), request.amount(), request.currency(), request.eventTimestamp(), traceId));
        return response(saved, false);
    }
    @Transactional(readOnly=true)
    public BalanceResponse balance(String accountId) {
        var txs=repository.findByAccountIdOrderByEventTimestampAsc(accountId);
        return new BalanceResponse(accountId, currency(txs), net(txs));
    }
    @Transactional(readOnly=true)
    public AccountResponse account(String accountId) {
        var txs=repository.findByAccountIdOrderByEventTimestampAsc(accountId);
        return new AccountResponse(accountId, currency(txs), net(txs), txs.stream().map(t->response(t,false)).toList());
    }
    private BigDecimal net(List<AccountTransaction> txs){return txs.stream().map(t->t.getType()==TransactionType.CREDIT?t.getAmount():t.getAmount().negate()).reduce(BigDecimal.ZERO, BigDecimal::add);}
    private String currency(List<AccountTransaction> txs){return txs.isEmpty()?null:txs.get(0).getCurrency();}
    private TransactionResponse response(AccountTransaction t, boolean duplicate){return new TransactionResponse(t.getEventId(),t.getAccountId(),t.getType(),t.getAmount(),t.getCurrency(),t.getEventTimestamp(),t.getTraceId(),duplicate);}
}
