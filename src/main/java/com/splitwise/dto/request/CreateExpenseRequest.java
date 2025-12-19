package com.splitwise.dto.request;

import com.splitwise.enums.SplitType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Map;

public class CreateExpenseRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Paid by user ID is required")
    private Long paidByUserId;

    private Long groupId;

    @NotNull(message = "Split type is required")
    private SplitType splitType;

    @NotEmpty(message = "At least one participant is required")
    private Map<Long, BigDecimal> splits;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getPaidByUserId() { return paidByUserId; }
    public void setPaidByUserId(Long paidByUserId) { this.paidByUserId = paidByUserId; }

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public SplitType getSplitType() { return splitType; }
    public void setSplitType(SplitType splitType) { this.splitType = splitType; }

    public Map<Long, BigDecimal> getSplits() { return splits; }
    public void setSplits(Map<Long, BigDecimal> splits) { this.splits = splits; }
}
