package com.centilontech.account.api;

import com.centilontech.account.domain.TransactionType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class AccountDtos {
    private AccountDtos() {}
    public record TransactionRequest(@NotBlank String eventId, @NotNull TransactionType type,
        @NotNull @DecimalMin(value="0.0", inclusive=false) BigDecimal amount,
        @NotBlank String currency, @NotNull Instant eventTimestamp) {}
    public record TransactionResponse(String eventId, String accountId, TransactionType type, BigDecimal amount,
        String currency, Instant eventTimestamp, String traceId, boolean duplicate) {}
    public record BalanceResponse(String accountId, String currency, BigDecimal balance) {}
    public record AccountResponse(String accountId, String currency, BigDecimal balance, List<TransactionResponse> transactions) {}
}
