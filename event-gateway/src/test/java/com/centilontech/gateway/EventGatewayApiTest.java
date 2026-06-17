package com.centilontech.gateway;

import com.centilontech.gateway.client.AccountServiceClient;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventGatewayApiTest {
  @Autowired
  MockMvc mvc;
  @MockitoBean
  AccountServiceClient client;

  private String event(String id, String type, String amount, String timestamp) {
    return """
        {"eventId":"%s","accountId":"acct-1","type":"%s","amount":%s,"currency":"USD","eventTimestamp":"%s","metadata":{"source":"test"}}
        """
        .formatted(id, type, amount, timestamp);
  }

  @Test
  void createsThenReturnsDuplicateWithoutSecondDownstreamCall() throws Exception {
    mvc.perform(post("/events").header("X-Trace-Id", "trace-1").contentType("application/json")
        .content(event("evt-1", "CREDIT", "10", "2026-05-15T14:00:00Z"))).andExpect(status().isCreated())
        .andExpect(header().string("X-Trace-Id", "trace-1"));
    mvc.perform(
        post("/events").contentType("application/json").content(event("evt-1", "CREDIT", "10", "2026-05-15T14:00:00Z")))
        .andExpect(status().isOk()).andExpect(jsonPath("$.duplicate").value(true));
    verify(client, times(1)).apply(eq("acct-1"), any(), eq("trace-1"));
  }

  @Test
  void ordersEventsAndValidatesInput() throws Exception {
    mvc.perform(
        post("/events").contentType("application/json").content(event("late", "DEBIT", "1", "2026-05-15T14:00:00Z")))
        .andExpect(status().isCreated());
    mvc.perform(
        post("/events").contentType("application/json").content(event("early", "CREDIT", "2", "2026-05-15T13:00:00Z")))
        .andExpect(status().isCreated());
    mvc.perform(get("/events").param("account", "acct-1")).andExpect(status().isOk())
        .andExpect(jsonPath("$[0].eventId").value("early")).andExpect(jsonPath("$[1].eventId").value("late"));
    mvc.perform(
        post("/events").contentType("application/json").content(event("bad", "CREDIT", "0", "2026-05-15T13:00:00Z")))
        .andExpect(status().isBadRequest());
    mvc.perform(
        post("/events").contentType("application/json").content(event("bad", "OTHER", "1", "2026-05-15T13:00:00Z")))
        .andExpect(status().isBadRequest());
  }

  @Test
  void mapsDownstreamFailureTo503AndDoesNotStoreEvent() throws Exception {
    doThrow(new com.centilontech.gateway.client.AccountServiceUnavailableException(new RuntimeException())).when(client)
        .apply(anyString(), any(), anyString());
    mvc.perform(post("/events").contentType("application/json")
        .content(event("failed", "CREDIT", "10", "2026-05-15T14:00:00Z"))).andExpect(status().isServiceUnavailable());
    mvc.perform(get("/events/failed")).andExpect(status().isNotFound());
  }
}
