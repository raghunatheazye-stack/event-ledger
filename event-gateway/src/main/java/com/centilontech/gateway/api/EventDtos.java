package com.centilontech.gateway.api;

import com.centilontech.gateway.domain.*; import jakarta.validation.constraints.*; import java.math.BigDecimal; import java.time.Instant; import java.util.Map;

public final class EventDtos {
 private EventDtos(){}
 public record EventRequest(@NotBlank String eventId,@NotBlank String accountId,@NotNull EventType type,@NotNull @DecimalMin(value="0.0",inclusive=false) BigDecimal amount,@NotBlank String currency,@NotNull Instant eventTimestamp,Map<String,Object> metadata){}
 public record EventResponse(String eventId,String accountId,EventType type,BigDecimal amount,String currency,Instant eventTimestamp,Map<String,Object> metadata,EventStatus status,String traceId,Instant createdAt,boolean duplicate){}
 public record AccountTransactionRequest(String eventId,EventType type,BigDecimal amount,String currency,Instant eventTimestamp){}
}
