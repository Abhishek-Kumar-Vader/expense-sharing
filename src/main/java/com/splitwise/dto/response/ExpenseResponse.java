package com.splitwise.dto.response;

import com.splitwise.enums.SplitType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ExpenseResponse {
    private Long id;
    private String description;
    private BigDecimal amount;
    private UserResponse paidBy;
    private Long groupId;
    private String groupName;
    private SplitType splitType;
    private List<SplitDetail> splits;
    private LocalDateTime createdAt;

    public ExpenseResponse() {}

    public ExpenseResponse(Long id, String description, BigDecimal amount, UserResponse paidBy,
                           Long groupId, String groupName, SplitType splitType,
                           List<SplitDetail> splits, LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.amount = amount;
        this.paidBy = paidBy;
        this.groupId = groupId;
        this.groupName = groupName;
        this.splitType = splitType;
        this.splits = splits;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public UserResponse getPaidBy() { return paidBy; }
    public Long getGroupId() { return groupId; }
    public String getGroupName() { return groupName; }
    public SplitType getSplitType() { return splitType; }
    public List<SplitDetail> getSplits() { return splits; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public static class SplitDetail {
        private Long userId;
        private String userName;
        private BigDecimal amount;
        private BigDecimal percentage;

        public SplitDetail() {}

        public SplitDetail(Long userId, String userName, BigDecimal amount, BigDecimal percentage) {
            this.userId = userId;
            this.userName = userName;
            this.amount = amount;
            this.percentage = percentage;
        }

        public Long getUserId() { return userId; }
        public String getUserName() { return userName; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getPercentage() { return percentage; }
    }
}
