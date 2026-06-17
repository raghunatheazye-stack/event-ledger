package com.centilontech.account.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
public class HealthController {
    private final JdbcTemplate jdbc;
    public HealthController(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @GetMapping("/health") Map<String,String> health(){jdbc.queryForObject("SELECT 1",Integer.class);return Map.of("service","account-service","status","UP","database","UP");}
}
