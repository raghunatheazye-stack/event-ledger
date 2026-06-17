package com.centilontech.gateway.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface LedgerEventRepository extends JpaRepository<LedgerEvent, Long> {
    Optional<LedgerEvent> findByEventId(String eventId);

    List<LedgerEvent> findByAccountIdOrderByEventTimestampAsc(String accountId);
}
