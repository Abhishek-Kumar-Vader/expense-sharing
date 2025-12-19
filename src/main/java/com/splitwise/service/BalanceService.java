package com.splitwise.service;

import com.splitwise.dto.response.BalanceResponse;
import com.splitwise.entity.Expense;
import com.splitwise.entity.Split;
import com.splitwise.entity.User;
import com.splitwise.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    public BalanceService(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public BalanceResponse getUserBalance(Long userId) {
        User user = userService.findUserById(userId);
        List<Expense> expenses = expenseRepository.findByUserId(userId);

        // Calculate net balances between all users
        Map<Long, BigDecimal> netBalances = calculateNetBalances(expenses);

        // Simplify transactions
        List<BalanceResponse.Transaction> transactions = simplifyDebts(netBalances);

        // Filter transactions involving this user
        List<BalanceResponse.Transaction> userTransactions = transactions.stream()
                .filter(t -> t.getFromUserId().equals(userId) || t.getToUserId().equals(userId))
                .collect(Collectors.toList());

        BigDecimal userNetBalance = netBalances.getOrDefault(userId, BigDecimal.ZERO);

        return new BalanceResponse(
                user.getId(),
                user.getName(),
                userNetBalance,
                userTransactions
        );
    }

    /**
     * Gets all balances across the system.
     */
    @Transactional(readOnly = true)
    public List<BalanceResponse> getAllBalances() {
        List<Expense> expenses = expenseRepository.findAll();
        Map<Long, BigDecimal> netBalances = calculateNetBalances(expenses);
        List<BalanceResponse.Transaction> transactions = simplifyDebts(netBalances);

        // Group transactions by user
        Map<Long, List<BalanceResponse.Transaction>> transactionsByUser = new HashMap<>();
        for (BalanceResponse.Transaction transaction : transactions) {
            transactionsByUser.computeIfAbsent(transaction.getFromUserId(), k -> new ArrayList<>())
                    .add(transaction);
            transactionsByUser.computeIfAbsent(transaction.getToUserId(), k -> new ArrayList<>())
                    .add(transaction);
        }

        return netBalances.entrySet().stream()
                .map(entry -> {
                    User user = userService.findUserById(entry.getKey());
                    List<BalanceResponse.Transaction> userTransactions =
                            transactionsByUser.getOrDefault(entry.getKey(), Collections.emptyList());

                    return new BalanceResponse(
                            user.getId(),
                            user.getName(),
                            entry.getValue(),
                            userTransactions
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Calculates net balance for each user.
     * Positive balance = others owe you
     * Negative balance = you owe others
     */
    private Map<Long, BigDecimal> calculateNetBalances(List<Expense> expenses) {
        Map<Long, BigDecimal> netBalances = new HashMap<>();

        for (Expense expense : expenses) {
            Long payerId = expense.getPaidBy().getId();

            // Payer gets credited the full amount
            netBalances.merge(payerId, expense.getAmount(), BigDecimal::add);

            // Each participant gets debited their share
            for (Split split : expense.getSplits()) {
                Long participantId = split.getUser().getId();
                netBalances.merge(participantId, split.getAmount().negate(), BigDecimal::add);
            }
        }

        return netBalances;
    }

    /**
     * Simplifies debts to minimize number of transactions.
     * Uses greedy algorithm: Match largest debtor with largest creditor.
     *
     * Algorithm Explanation:
     * 1. Separate users into debtors (owe money) and creditors (are owed money)
     * 2. Sort both lists by amount
     * 3. Match largest debtor with largest creditor
     * 4. Create transaction for minimum of the two amounts
     * 5. Update balances and repeat until all debts settled
     *
     * Time Complexity: O(n log n) where n is number of users
     * This algorithm minimizes transactions to at most (n-1) transactions.
     */
    private List<BalanceResponse.Transaction> simplifyDebts(Map<Long, BigDecimal> netBalances) {
        List<BalanceResponse.Transaction> transactions = new ArrayList<>();

        // Separate debtors and creditors
        List<UserBalance> debtors = new ArrayList<>();
        List<UserBalance> creditors = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : netBalances.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                // Negative balance = owes money
                User user = userService.findUserById(entry.getKey());
                debtors.add(new UserBalance(user, entry.getValue().negate()));
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                // Positive balance = is owed money
                User user = userService.findUserById(entry.getKey());
                creditors.add(new UserBalance(user, entry.getValue()));
            }
        }

        // Sort in descending order
        debtors.sort((a, b) -> b.balance.compareTo(a.balance));
        creditors.sort((a, b) -> b.balance.compareTo(a.balance));

        // Greedy matching: largest debtor with largest creditor
        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            UserBalance debtor = debtors.get(i);
            UserBalance creditor = creditors.get(j);

            // Transaction amount is minimum of debt and credit
            BigDecimal transactionAmount = debtor.balance.min(creditor.balance);

            transactions.add(new BalanceResponse.Transaction(
                    debtor.user.getId(),
                    debtor.user.getName(),
                    creditor.user.getId(),
                    creditor.user.getName(),
                    transactionAmount
            ));

            // Update balances
            debtor.balance = debtor.balance.subtract(transactionAmount);
            creditor.balance = creditor.balance.subtract(transactionAmount);

            // Move to next if balance is settled
            if (debtor.balance.compareTo(BigDecimal.ZERO) == 0) {
                i++;
            }
            if (creditor.balance.compareTo(BigDecimal.ZERO) == 0) {
                j++;
            }
        }

        return transactions;
    }

    /**
     * Helper class for debt simplification algorithm.
     */
    private static class UserBalance {
        User user;
        BigDecimal balance;

        UserBalance(User user, BigDecimal balance) {
            this.user = user;
            this.balance = balance;
        }
    }
}
