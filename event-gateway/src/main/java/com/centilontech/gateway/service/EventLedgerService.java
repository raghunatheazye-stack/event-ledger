package com.centilontech.gateway.service;

import com.centilontech.gateway.api.EventDtos.*;
import com.centilontech.gateway.client.AccountServiceClient;
import com.centilontech.gateway.domain.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class EventLedgerService {
  private final LedgerEventRepository repository;
  private final AccountServiceClient client;
  private final ObjectMapper mapper;
  private final Counter submitted, duplicates, failed;

  public EventLedgerService(LedgerEventRepository repository, AccountServiceClient client, ObjectMapper mapper,
      MeterRegistry metrics) {
    this.repository = repository;
    this.client = client;
    this.mapper = mapper;
    this.submitted = metrics.counter("events.submitted");
    this.duplicates = metrics.counter("events.duplicates");
    this.failed = metrics.counter("events.failed");
  }

  @Transactional
  public EventResponse submit(EventRequest request, String traceId) {
    var existing = repository.findByEventId(request.eventId());
    if (existing.isPresent()) {
      duplicates.increment();
      return response(existing.get(), true);
    }
    try {
      client.apply(request.accountId(), request, traceId);
    } catch (RuntimeException e) {
      failed.increment();
      throw e;
    }
    var saved = repository.saveAndFlush(new LedgerEvent(request.eventId(), request.accountId(), request.type(),
        request.amount(), request.currency(), request.eventTimestamp(), json(request.metadata()), traceId));
    submitted.increment();
    return response(saved, false);
  }

  @Transactional(readOnly = true)
  public EventResponse get(String eventId) {
    return repository.findByEventId(eventId).map(e -> response(e, false))
        .orElseThrow(() -> new EventNotFoundException(eventId));
  }

  @Transactional(readOnly = true)
  public List<EventResponse> list(String accountId) {
    return repository.findByAccountIdOrderByEventTimestampAsc(accountId).stream().map(e -> response(e, false)).toList();
  }

  private String json(Map<String, Object> metadata) {
    try {
      return metadata == null ? null : mapper.writeValueAsString(metadata);
    } catch (Exception e) {
      throw new IllegalArgumentException("metadata must be valid JSON", e);
    }
  }

  private Map<String, Object> metadata(String json) {
    try {
      return json == null ? null : mapper.readValue(json, new TypeReference<>() {
      });
    } catch (Exception e) {
      return Map.of();
    }
  }

  private EventResponse response(LedgerEvent e, boolean duplicate) {
    return new EventResponse(e.getEventId(), e.getAccountId(), e.getType(), e.getAmount(), e.getCurrency(),
        e.getEventTimestamp(), metadata(e.getMetadataJson()), e.getStatus(), e.getTraceId(), e.getCreatedAt(),
        duplicate);
  }

  public static class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String id) {
      super("Event not found: " + id);
    }
  }
}
