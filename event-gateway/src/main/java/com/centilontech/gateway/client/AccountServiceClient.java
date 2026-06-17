package com.centilontech.gateway.client;

import com.centilontech.gateway.api.EventDtos.*;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.MediaType; import org.springframework.http.client.SimpleClientHttpRequestFactory; import org.springframework.stereotype.Component; import org.springframework.web.client.RestClient;
import java.time.Duration;

@Component
public class AccountServiceClient {
 private final RestClient client;
 public AccountServiceClient(RestClient.Builder builder,@Value("${account-service.base-url}") String baseUrl,@Value("${account-service.timeout:2s}") Duration timeout){
   var factory=new SimpleClientHttpRequestFactory(); factory.setConnectTimeout(timeout); factory.setReadTimeout(timeout);
   this.client=builder.baseUrl(baseUrl).requestFactory(factory).build();
 }
 @Retry(name="accountService",fallbackMethod="unavailable")
 public void apply(String accountId,EventRequest event,String traceId){client.post().uri("/accounts/{accountId}/transactions",accountId).header("X-Trace-Id",traceId).contentType(MediaType.APPLICATION_JSON).body(new AccountTransactionRequest(event.eventId(),event.type(),event.amount(),event.currency(),event.eventTimestamp())).retrieve().toBodilessEntity();}
 void unavailable(String accountId,EventRequest event,String traceId,Throwable cause){throw new AccountServiceUnavailableException(cause);}
}
