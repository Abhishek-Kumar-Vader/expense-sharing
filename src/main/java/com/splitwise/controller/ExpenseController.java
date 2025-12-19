package com.splitwise.controller;

import com.splitwise.dto.request.CreateExpenseRequest;
import com.splitwise.dto.response.ExpenseResponse;
import com.splitwise.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getAllExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    /**
     * @RequestParam: Extracts query parameters
     * Example: GET /api/expenses?groupId=5
     * required = false: Makes parameter optional
     */
    @GetMapping("/by-group")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByGroup(
            @RequestParam Long groupId) {
        return ResponseEntity.ok(expenseService.getExpensesByGroup(groupId));
    }

    @GetMapping("/by-user")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByUser(
            @RequestParam Long userId) {
        return ResponseEntity.ok(expenseService.getExpensesByUser(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ExpenseResponse> createExpense(
            @Valid @RequestBody CreateExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(expenseService.createExpense(request));
    }
}
