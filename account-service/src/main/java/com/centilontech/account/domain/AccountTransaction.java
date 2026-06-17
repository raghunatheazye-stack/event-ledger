package com.centilontech.account.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "account_transactions", uniqueConstraints = @UniqueConstraint(name = "uk_transaction_event_id", columnNames = "event_id"))
public class AccountTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="event_id", nullable=false, updatable=false) private String eventId;
    @Column(nullable=false, updatable=false) private String accountId;
    @Enumerated(EnumType.STRING) @Column(nullable=false, updatable=false) private TransactionType type;
    @Column(nullable=false, precision=19, scale=4, updatable=false) private BigDecimal amount;
    @Column(nullable=false, updatable=false) private String currency;
    @Column(nullable=false, updatable=false) private Instant eventTimestamp;
    @Column(updatable=false) private String traceId;
    @Column(nullable=false, updatable=false) private Instant createdAt;

    protected AccountTransaction() {}
    public AccountTransaction(String eventId, String accountId, TransactionType type, BigDecimal amount, String currency, Instant eventTimestamp, String traceId) {
        this.eventId=eventId; this.accountId=accountId; this.type=type; this.amount=amount; this.currency=currency;
        this.eventTimestamp=eventTimestamp; this.traceId=traceId; this.createdAt=Instant.now();
    }
    public Long getId(){return id;} public String getEventId(){return eventId;} public String getAccountId(){return accountId;}
    public TransactionType getType(){return type;} public BigDecimal getAmount(){return amount;} public String getCurrency(){return currency;}
    public Instant getEventTimestamp(){return eventTimestamp;} public String getTraceId(){return traceId;} public Instant getCreatedAt(){return createdAt;}
}
