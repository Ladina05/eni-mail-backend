package mg.eni.mail.controller;

import jakarta.validation.Valid;
import mg.eni.mail.dto.GroupRequest;
import mg.eni.mail.dto.GroupResponse;
import mg.eni.mail.dto.UserResponse;
import mg.eni.mail.entity.*;
import mg.eni.mail.exception.ApiException;
import mg.eni.mail.repository.*;
import mg.eni.mail.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@RestController
@RequestMapping("/api/groups")
public class GroupController {
    private final GroupRepository groups;
    private final GroupMemberRepository members;
    private final UserRepository users;

    public GroupController(GroupRepository groups, GroupMemberRepository members, UserRepository users) {
        this.groups = groups; this.members = members; this.users = users;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<GroupResponse> all(Authentication auth) {
        return groups.findAll().stream()
            .filter(group -> group.getCreatedBy().getEmail().equalsIgnoreCase(auth.getName())
                || group.getCreatedBy().getRole() == Role.ADMIN)
            .map(this::toResponse).toList();
    }

    @PostMapping
    public GroupResponse create(@Valid @RequestBody GroupRequest request, Authentication auth) {
        User creator = users.findByEmailIgnoreCase(auth.getName())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        Group group = new Group();
        group.setName(request.name().trim());
        group.setDescription(request.description());
        group.setCreatedBy(creator);
        return toResponse(groups.save(group));
    }

    @PostMapping("/{groupId}/members/{userId}")
    public Map<String,String> addMember(@PathVariable UUID groupId, @PathVariable UUID userId, Authentication auth) {
        Group group = ownedGroup(groupId, auth.getName());
        User user = users.findById(userId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Utilisateur introuvable"));
        if (!members.existsByGroupIdAndUserId(groupId, userId)) {
            GroupMember gm = new GroupMember(); gm.setGroup(group); gm.setUser(user); members.save(gm);
        }
        return Map.of("message", "Membre ajouté");
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public Map<String,String> removeMember(@PathVariable UUID groupId, @PathVariable UUID userId, Authentication auth) {
        readableGroup(groupId, auth.getName());
        members.deleteByGroupIdAndUserId(groupId, userId);
        return Map.of("message", "Membre retiré");
    }

    @GetMapping("/{groupId}/members")
    @Transactional(readOnly = true)
    public List<UserResponse> members(@PathVariable UUID groupId, Authentication auth) {
        readableGroup(groupId, auth.getName());
        return members.findByGroupId(groupId).stream()
            .map(GroupMember::getUser).map(AuthService::toResponse).toList();
    }

    private Group ownedGroup(UUID id, String email) {
        Group group = groups.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Groupe introuvable"));
        if (!group.getCreatedBy().getEmail().equalsIgnoreCase(email))
            throw new ApiException(HttpStatus.FORBIDDEN, "Accès refusé : vous ne gérez pas ce groupe");
        return group;
    }

    private Group readableGroup(UUID id, String email) {
        Group group = groups.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Groupe introuvable"));
        if (!group.getCreatedBy().getEmail().equalsIgnoreCase(email) && group.getCreatedBy().getRole() != Role.ADMIN)
            throw new ApiException(HttpStatus.FORBIDDEN, "Accès refusé à ce groupe");
        return group;
    }

    private GroupResponse toResponse(Group group) {
        return new GroupResponse(group.getId(), group.getName(), group.getDescription(),
            group.getCreatedBy().getEmail(), group.getCreatedAt());
    }
}
