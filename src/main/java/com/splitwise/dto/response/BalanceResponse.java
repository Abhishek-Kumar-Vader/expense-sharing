package com.splitwise.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class BalanceResponse {
    private Long userId;
    private String userName;
    private BigDecimal netBalance;
    private List<Transaction> transactions;

    public BalanceResponse() {}

    public BalanceResponse(Long userId, String userName, BigDecimal netBalance, List<Transaction> transactions) {
        this.userId = userId;
        this.userName = userName;
        this.netBalance = netBalance;
        this.transactions = transactions;
    }

    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public BigDecimal getNetBalance() { return netBalance; }
    public List<Transaction> getTransactions() { return transactions; }

    public static class Transaction {
        private Long fromUserId;
        private String fromUserName;
        private Long toUserId;
        private String toUserName;
        private BigDecimal amount;

        public Transaction() {}

        public Transaction(Long fromUserId, String fromUserName, Long toUserId, String toUserName, BigDecimal amount) {
            this.fromUserId = fromUserId;
            this.fromUserName = fromUserName;
            this.toUserId = toUserId;
            this.toUserName = toUserName;
            this.amount = amount;
        }

        public Long getFromUserId() { return fromUserId; }
        public String getFromUserName() { return fromUserName; }
        public Long getToUserId() { return toUserId; }
        public String getToUserName() { return toUserName; }
        public BigDecimal getAmount() { return amount; }

        @Override
        public String toString() {
            return String.format("%s owes %s $%.2f", fromUserName, toUserName, amount);
        }
    }
}
