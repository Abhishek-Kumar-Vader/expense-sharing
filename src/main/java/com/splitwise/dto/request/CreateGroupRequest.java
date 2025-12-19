package com.splitwise.dto.request;

import jakarta.validation.constraints.*;
import java.util.Set;

public class CreateGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(min = 2, max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @NotEmpty(message = "At least one member is required")
    private Set<Long> memberIds;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<Long> getMemberIds() { return memberIds; }
    public void setMemberIds(Set<Long> memberIds) { this.memberIds = memberIds; }
}
