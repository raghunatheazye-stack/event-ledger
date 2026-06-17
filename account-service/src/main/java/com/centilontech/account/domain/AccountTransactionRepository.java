package com.centilontech.account.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {
    Optional<AccountTransaction> findByEventId(String eventId);

    List<AccountTransaction> findByAccountIdOrderByEventTimestampAsc(String accountId);
}
