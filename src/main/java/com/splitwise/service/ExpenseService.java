package com.splitwise.service;

import com.splitwise.dto.request.CreateExpenseRequest;
import com.splitwise.dto.response.ExpenseResponse;
import com.splitwise.dto.response.UserResponse;
import com.splitwise.entity.Expense;
import com.splitwise.entity.Group;
import com.splitwise.entity.Split;
import com.splitwise.entity.User;
import com.splitwise.enums.SplitType;
import com.splitwise.exception.InvalidExpenseException;
import com.splitwise.exception.ResourceNotFoundException;
import com.splitwise.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    private final GroupService groupService;

    public ExpenseService(ExpenseRepository expenseRepository, UserService userService, GroupService groupService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
        this.groupService = groupService;
    }

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request) {
        // Validate and fetch entities
        User paidBy = userService.findUserById(request.getPaidByUserId());
        Group group = null;
        if (request.getGroupId() != null) {
            group = groupService.findGroupById(request.getGroupId());
            // Validate payer is in group
            if (!group.getMembers().contains(paidBy)) {
                throw new InvalidExpenseException("Payer must be a member of the group");
            }
        }

        // Create expense
        Expense expense = new Expense();
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setPaidBy(paidBy);
        expense.setGroup(group);
        expense.setSplitType(request.getSplitType());

        // Create splits based on split type
        List<Split> splits = createSplits(expense, request, group);
        expense.setSplits(splits);

        // Validate total
        validateSplits(expense);

        Expense savedExpense = expenseRepository.save(expense);
        return convertToResponse(savedExpense);
    }

    private List<Split> createSplits(Expense expense, CreateExpenseRequest request, Group group) {
        List<Split> splits = new ArrayList<>();

        switch (request.getSplitType()) {
            case EQUAL:
                splits = createEqualSplits(expense, request, group);
                break;
            case EXACT:
                splits = createExactSplits(expense, request);
                break;
            case PERCENTAGE:
                splits = createPercentageSplits(expense, request);
                break;
        }

        return splits;
    }

    private List<Split> createEqualSplits(Expense expense, CreateExpenseRequest request, Group group) {
        List<Split> splits = new ArrayList<>();
        Map<Long, BigDecimal> splitMap = request.getSplits();

        int participantCount = splitMap.size();
        BigDecimal equalShare = expense.getAmount()
                .divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.HALF_UP);

        // Handle rounding: Last person gets the remainder
        BigDecimal totalAssigned = BigDecimal.ZERO;
        List<Long> userIds = new ArrayList<>(splitMap.keySet());

        for (int i = 0; i < userIds.size(); i++) {
            Long userId = userIds.get(i);
            User user = userService.findUserById(userId);

            // Validate user is in group if group expense
            if (group != null && !group.getMembers().contains(user)) {
                throw new InvalidExpenseException("User " + userId + " is not a member of the group");
            }

            Split split = new Split();
            split.setExpense(expense);
            split.setUser(user);

            // Last participant gets exact remainder to handle rounding
            if (i == userIds.size() - 1) {
                split.setAmount(expense.getAmount().subtract(totalAssigned));
            } else {
                split.setAmount(equalShare);
                totalAssigned = totalAssigned.add(equalShare);
            }

            splits.add(split);
        }

        return splits;
    }

    private List<Split> createExactSplits(Expense expense, CreateExpenseRequest request) {
        List<Split> splits = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : request.getSplits().entrySet()) {
            User user = userService.findUserById(entry.getKey());

            Split split = new Split();
            split.setExpense(expense);
            split.setUser(user);
            split.setAmount(entry.getValue());

            splits.add(split);
        }

        return splits;
    }

    private List<Split> createPercentageSplits(Expense expense, CreateExpenseRequest request) {
        List<Split> splits = new ArrayList<>();
        BigDecimal totalAssigned = BigDecimal.ZERO;

        List<Map.Entry<Long, BigDecimal>> entries = new ArrayList<>(request.getSplits().entrySet());

        for (int i = 0; i < entries.size(); i++) {
            Map.Entry<Long, BigDecimal> entry = entries.get(i);
            User user = userService.findUserById(entry.getKey());
            BigDecimal percentage = entry.getValue();

            Split split = new Split();
            split.setExpense(expense);
            split.setUser(user);
            split.setPercentage(percentage);

            // Calculate amount from percentage
            // Last participant gets exact remainder to handle rounding
            if (i == entries.size() - 1) {
                split.setAmount(expense.getAmount().subtract(totalAssigned));
            } else {
                BigDecimal amount = expense.getAmount()
                        .multiply(percentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                split.setAmount(amount);
                totalAssigned = totalAssigned.add(amount);
            }

            splits.add(split);
        }

        return splits;
    }

    private void validateSplits(Expense expense) {
        BigDecimal totalSplitAmount = expense.getSplits().stream()
                .map(Split::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Allow 1 cent tolerance for rounding
        BigDecimal difference = expense.getAmount().subtract(totalSplitAmount).abs();
        if (difference.compareTo(new BigDecimal("0.01")) > 0) {
            throw new InvalidExpenseException(
                    String.format("Split amounts ($%.2f) do not match expense amount ($%.2f)",
                            totalSplitAmount, expense.getAmount())
            );
        }

        if (expense.getSplitType() == SplitType.PERCENTAGE) {
            BigDecimal totalPercentage = expense.getSplits().stream()
                    .map(Split::getPercentage)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
                throw new InvalidExpenseException(
                        "Percentages must sum to 100%, got: " + totalPercentage
                );
            }
        }
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByGroup(Long groupId) {
        return expenseRepository.findByGroupId(groupId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByUser(Long userId) {
        return expenseRepository.findByUserId(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private ExpenseResponse convertToResponse(Expense expense) {
        UserResponse paidByResponse = new UserResponse(
                expense.getPaidBy().getId(),
                expense.getPaidBy().getName(),
                expense.getPaidBy().getEmail(),
                expense.getPaidBy().getPhoneNumber(),
                expense.getPaidBy().getCreatedAt()
        );

        List<ExpenseResponse.SplitDetail> splitDetails = expense.getSplits().stream()
                .map(split -> new ExpenseResponse.SplitDetail(
                        split.getUser().getId(),
                        split.getUser().getName(),
                        split.getAmount(),
                        split.getPercentage()
                ))
                .collect(Collectors.toList());

        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                paidByResponse,
                expense.getGroup() != null ? expense.getGroup().getId() : null,
                expense.getGroup() != null ? expense.getGroup().getName() : null,
                expense.getSplitType(),
                splitDetails,
                expense.getCreatedAt()
        );
    }
}
