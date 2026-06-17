package com.centilontech.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AccountApiTest {
  @Autowired
  MockMvc mvc;
  @Autowired
  ObjectMapper mapper;

  private String tx(String id, String type, String amount, String timestamp) {
    return """
        {"eventId":"%s","type":"%s","amount":%s,"currency":"USD","eventTimestamp":"%s"}
        """.formatted(id, type, amount, timestamp);
  }

  @Test
  void computesBalanceHandlesDuplicatesAndOrdersTransactions() throws Exception {
    mvc.perform(post("/accounts/acct-1/transactions").header("X-Trace-Id", "trace-123").contentType("application/json")
        .content(tx("evt-2", "DEBIT", "50", "2026-05-15T14:00:00Z"))).andExpect(status().isCreated())
        .andExpect(jsonPath("$.traceId").value("trace-123"));
    mvc.perform(post("/accounts/acct-1/transactions").contentType("application/json")
        .content(tx("evt-1", "CREDIT", "150", "2026-05-15T13:00:00Z"))).andExpect(status().isCreated());
    mvc.perform(post("/accounts/acct-1/transactions").contentType("application/json")
        .content(tx("evt-1", "CREDIT", "150", "2026-05-15T13:00:00Z"))).andExpect(status().isOk())
        .andExpect(jsonPath("$.duplicate").value(true));
    mvc.perform(get("/accounts/acct-1/balance")).andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(100.0));
    mvc.perform(get("/accounts/acct-1")).andExpect(status().isOk())
        .andExpect(jsonPath("$.transactions[0].eventId").value("evt-1"))
        .andExpect(jsonPath("$.transactions[1].eventId").value("evt-2"));
  }

  @Test
  void rejectsZeroAmountAndUnknownType() throws Exception {
    mvc.perform(post("/accounts/acct-1/transactions").contentType("application/json")
        .content(tx("bad", "CREDIT", "0", "2026-05-15T13:00:00Z"))).andExpect(status().isBadRequest());
    mvc.perform(post("/accounts/acct-1/transactions").contentType("application/json")
        .content(tx("bad", "OTHER", "1", "2026-05-15T13:00:00Z"))).andExpect(status().isBadRequest());
  }
}
