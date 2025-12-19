package com.splitwise.service;

import com.splitwise.dto.request.CreateGroupRequest;
import com.splitwise.dto.response.GroupResponse;
import com.splitwise.dto.response.UserResponse;
import com.splitwise.entity.Group;
import com.splitwise.entity.User;
import com.splitwise.exception.ResourceNotFoundException;
import com.splitwise.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;

    public GroupService(GroupRepository groupRepository, UserService userService) {
        this.groupRepository = groupRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(Long id) {
        Group group = findGroupById(id);
        return convertToResponse(group);
    }

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());

        // Fetch and add members
        Set<User> members = new HashSet<>();
        for (Long userId : request.getMemberIds()) {
            User user = userService.findUserById(userId);
            members.add(user);
        }
        group.setMembers(members);

        Group savedGroup = groupRepository.save(group);
        return convertToResponse(savedGroup);
    }

    @Transactional(readOnly = true)
    public Group findGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
    }

    private GroupResponse convertToResponse(Group group) {
        Set<UserResponse> members = group.getMembers().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getCreatedAt()
                ))
                .collect(Collectors.toSet());

        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                members,
                group.getCreatedAt()
        );
    }
}
