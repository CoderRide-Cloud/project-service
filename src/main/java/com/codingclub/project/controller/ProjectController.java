package com.codingclub.project.controller;

import com.codingclub.common.exception.UnauthorizedException;
import com.codingclub.common.security.AuthUserContext;
import com.codingclub.common.security.AuthorizationService;
import com.codingclub.common.security.Permission;
import com.codingclub.common.web.AuthContextResolver;
import com.codingclub.project.dto.ProjectRequest;
import com.codingclub.project.model.Project;
import com.codingclub.project.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private AuthContextResolver authContextResolver;

    @Autowired
    private AuthorizationService authorizationService;

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects(@RequestParam(required = false) Boolean approvedOnly) {
        if (Boolean.TRUE.equals(approvedOnly)) {
            return ResponseEntity.ok(projectService.getApprovedProjects());
        }
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Project>> getProjectsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(projectService.getProjectsByUserId(userId));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Project>> getPendingProjects(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Permissions", required = false) String permissions,
            @RequestHeader(value = "X-User-Position", required = false) String position,
            @RequestHeader(value = "X-User-Is-Lead", required = false) String isLead,
            @RequestHeader(value = "X-User-Is-Active", required = false) String isActive,
            @RequestHeader(value = "X-User-Custom-Role-Id", required = false) String customRoleId) {

        AuthUserContext authUser = authContextResolver.resolve(userId, role, permissions, position, isLead, isActive, customRoleId);
        authorizationService.requirePermission(authUser, Permission.MANAGE_PROJECTS);
        return ResponseEntity.ok(projectService.getPendingProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    public ResponseEntity<Project> createProject(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Permissions", required = false) String permissions,
            @RequestHeader(value = "X-User-Position", required = false) String position,
            @RequestHeader(value = "X-User-Is-Lead", required = false) String isLead,
            @RequestHeader(value = "X-User-Is-Active", required = false) String isActive,
            @RequestHeader(value = "X-User-Custom-Role-Id", required = false) String customRoleId,
            @RequestBody ProjectRequest request) {

        AuthUserContext authUser = authContextResolver.resolve(userId, role, permissions, position, isLead, isActive, customRoleId);
        authorizationService.requireActive(authUser);

        return ResponseEntity.ok(projectService.createProject(
                authUser.getUserId(),
                request.toProject(),
                request.getTags(),
                request.getSubmitterName(),
                request.getSubmitterEmail()
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Permissions", required = false) String permissions,
            @RequestHeader(value = "X-User-Position", required = false) String position,
            @RequestHeader(value = "X-User-Is-Lead", required = false) String isLead,
            @RequestHeader(value = "X-User-Is-Active", required = false) String isActive,
            @RequestHeader(value = "X-User-Custom-Role-Id", required = false) String customRoleId,
            @PathVariable Long id,
            @RequestBody ProjectRequest request) {

        AuthUserContext authUser = authContextResolver.resolve(userId, role, permissions, position, isLead, isActive, customRoleId);
        authorizationService.requireActive(authUser);

        Project existing = projectService.getProjectById(id);
        if (!existing.getMemberId().equals(authUser.getUserId()) && !authUser.isAdmin()) {
            throw new UnauthorizedException("Not authorized to update this project");
        }

        return ResponseEntity.ok(projectService.updateProject(
                id,
                request.toProject(),
                request.getTags(),
                request.getSubmitterName(),
                request.getSubmitterEmail()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Permissions", required = false) String permissions,
            @RequestHeader(value = "X-User-Position", required = false) String position,
            @RequestHeader(value = "X-User-Is-Lead", required = false) String isLead,
            @RequestHeader(value = "X-User-Is-Active", required = false) String isActive,
            @RequestHeader(value = "X-User-Custom-Role-Id", required = false) String customRoleId,
            @PathVariable Long id) {

        AuthUserContext authUser = authContextResolver.resolve(userId, role, permissions, position, isLead, isActive, customRoleId);
        authorizationService.requireActive(authUser);

        Project existing = projectService.getProjectById(id);
        if (!existing.getMemberId().equals(authUser.getUserId()) && !authUser.isAdmin()) {
            throw new UnauthorizedException("Not authorized to delete this project");
        }

        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approval")
    public ResponseEntity<Project> approveProject(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Permissions", required = false) String permissions,
            @RequestHeader(value = "X-User-Position", required = false) String position,
            @RequestHeader(value = "X-User-Is-Lead", required = false) String isLead,
            @RequestHeader(value = "X-User-Is-Active", required = false) String isActive,
            @RequestHeader(value = "X-User-Custom-Role-Id", required = false) String customRoleId,
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {

        AuthUserContext authUser = authContextResolver.resolve(userId, role, permissions, position, isLead, isActive, customRoleId);
        authorizationService.requirePermission(authUser, Permission.MANAGE_PROJECTS);

        Boolean isApproved = (Boolean) payload.get("isApproved");
        String reason = (String) payload.get("reason");
        String submitterName = (String) payload.get("submitterName");
        String submitterEmail = (String) payload.get("submitterEmail");

        return ResponseEntity.ok(projectService.approveProject(id, isApproved, reason, submitterName, submitterEmail));
    }
}
