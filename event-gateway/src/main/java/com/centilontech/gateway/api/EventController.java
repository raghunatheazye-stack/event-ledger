package com.centilontech.gateway.api;

import com.centilontech.gateway.api.EventDtos.*; import com.centilontech.gateway.service.EventLedgerService; import jakarta.validation.Valid; import org.slf4j.MDC; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.util.List;

@RestController
public class EventController {
 private final EventLedgerService service; public EventController(EventLedgerService service){this.service=service;}
 @PostMapping("/events") ResponseEntity<EventResponse> submit(@Valid @RequestBody EventRequest request){var result=service.submit(request,MDC.get("traceId"));return ResponseEntity.status(result.duplicate()?HttpStatus.OK:HttpStatus.CREATED).body(result);}
 @GetMapping("/events/{id}") EventResponse get(@PathVariable String id){return service.get(id);}
 @GetMapping(value="/events",params="account") List<EventResponse> list(@RequestParam("account") String accountId){return service.list(accountId);}
}
