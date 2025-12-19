package com.splitwise.controller;

import com.splitwise.dto.response.BalanceResponse;
import com.splitwise.service.BalanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/balances")
public class BalanceController {

    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping
    public ResponseEntity<List<BalanceResponse>> getAllBalances() {
        return ResponseEntity.ok(balanceService.getAllBalances());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<BalanceResponse> getUserBalance(@PathVariable Long userId) {
        return ResponseEntity.ok(balanceService.getUserBalance(userId));
    }
}
