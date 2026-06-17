package com.centilontech.gateway.domain;

import jakarta.persistence.*; import java.math.BigDecimal; import java.time.Instant;

@Entity
@Table(name="ledger_events",uniqueConstraints=@UniqueConstraint(name="uk_ledger_event_id",columnNames="event_id"))
public class LedgerEvent {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="event_id",nullable=false,updatable=false) private String eventId;
 @Column(nullable=false,updatable=false) private String accountId;
 @Enumerated(EnumType.STRING) @Column(nullable=false,updatable=false) private EventType type;
 @Column(nullable=false,precision=19,scale=4,updatable=false) private BigDecimal amount;
 @Column(nullable=false,updatable=false) private String currency;
 @Column(nullable=false,updatable=false) private Instant eventTimestamp;
 @Lob @Column(updatable=false) private String metadataJson;
 @Enumerated(EnumType.STRING) @Column(nullable=false,updatable=false) private EventStatus status;
 @Column(nullable=false,updatable=false) private String traceId;
 @Column(nullable=false,updatable=false) private Instant createdAt;
 protected LedgerEvent(){}
 public LedgerEvent(String eventId,String accountId,EventType type,BigDecimal amount,String currency,Instant eventTimestamp,String metadataJson,String traceId){this.eventId=eventId;this.accountId=accountId;this.type=type;this.amount=amount;this.currency=currency;this.eventTimestamp=eventTimestamp;this.metadataJson=metadataJson;this.status=EventStatus.APPLIED;this.traceId=traceId;this.createdAt=Instant.now();}
 public String getEventId(){return eventId;} public String getAccountId(){return accountId;} public EventType getType(){return type;} public BigDecimal getAmount(){return amount;} public String getCurrency(){return currency;} public Instant getEventTimestamp(){return eventTimestamp;} public String getMetadataJson(){return metadataJson;} public EventStatus getStatus(){return status;} public String getTraceId(){return traceId;} public Instant getCreatedAt(){return createdAt;}
}
