package com.centilontech.gateway;

import com.centilontech.account.AccountServiceApplication;
import org.junit.jupiter.api.*;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.*;
import org.springframework.web.client.RestClient;
import static org.assertj.core.api.Assertions.assertThat;

class FullFlowIntegrationTest {
  private ConfigurableApplicationContext account, gateway;

  @AfterEach
  void close() {
    if (gateway != null)
      gateway.close();
    if (account != null)
      account.close();
  }

  @Test
  void gatewayAppliesTransactionAndBalanceIsQueryable() {
    account = new SpringApplicationBuilder(AccountServiceApplication.class).run("--server.port=0",
        "--spring.datasource.url=jdbc:h2:mem:integration-accounts;DB_CLOSE_DELAY=-1");
    int accountPort = ((WebServerApplicationContext) account).getWebServer().getPort();
    gateway = new SpringApplicationBuilder(EventGatewayApplication.class).run("--server.port=0",
        "--account-service.base-url=http://localhost:" + accountPort,
        "--spring.datasource.url=jdbc:h2:mem:integration-events;DB_CLOSE_DELAY=-1");
    int gatewayPort = ((WebServerApplicationContext) gateway).getWebServer().getPort();
    var http = RestClient.create();
    String payload = "{\"eventId\":\"integration-1\",\"accountId\":\"acct-int\",\"type\":\"CREDIT\",\"amount\":42.50,\"currency\":\"USD\",\"eventTimestamp\":\"2026-05-15T14:00:00Z\"}";
    var created = http.post().uri("http://localhost:" + gatewayPort + "/events")
        .header("X-Trace-Id", "integration-trace").contentType(MediaType.APPLICATION_JSON).body(payload).retrieve()
        .toEntity(String.class);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(created.getHeaders().getFirst("X-Trace-Id")).isEqualTo("integration-trace");
    String balance = http.get().uri("http://localhost:" + accountPort + "/accounts/acct-int/balance").retrieve()
        .body(String.class);
    assertThat(balance).contains("42.50");
    String accountDetails = http.get().uri("http://localhost:" + accountPort + "/accounts/acct-int").retrieve()
        .body(String.class);
    assertThat(accountDetails).contains("integration-trace");
  }
}
