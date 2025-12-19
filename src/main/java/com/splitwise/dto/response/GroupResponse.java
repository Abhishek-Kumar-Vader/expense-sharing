package com.splitwise.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public class GroupResponse {
    private Long id;
    private String name;
    private String description;
    private Set<UserResponse> members;
    private LocalDateTime createdAt;

    public GroupResponse() {}

    public GroupResponse(Long id, String name, String description, Set<UserResponse> members, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.members = members;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<UserResponse> getMembers() { return members; }
    public void setMembers(Set<UserResponse> members) { this.members = members; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
