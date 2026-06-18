package com.codingclub.project.service;

import com.codingclub.common.exception.ResourceNotFoundException;
import com.codingclub.project.model.Project;
import com.codingclub.project.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getApprovedProjects() {
        return projectRepository.findByIsApprovedTrue();
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));
    }

    public Project createProject(Long memberId, Project project) {
        project.setMemberId(memberId);
        project.setIsApproved(false);
        project.setIsRejected(false);
        return projectRepository.save(project);
    }

    public Project updateProject(Long id, Project updatedData) {
        Project existingProject = getProjectById(id);
        
        if (updatedData.getTitle() != null) existingProject.setTitle(updatedData.getTitle());
        if (updatedData.getDescription() != null) existingProject.setDescription(updatedData.getDescription());
        if (updatedData.getGithubUrl() != null) existingProject.setGithubUrl(updatedData.getGithubUrl());
        if (updatedData.getLiveUrl() != null) existingProject.setLiveUrl(updatedData.getLiveUrl());
        if (updatedData.getImageUrl() != null) existingProject.setImageUrl(updatedData.getImageUrl());
        if (updatedData.getCategory() != null) existingProject.setCategory(updatedData.getCategory());
        
        return projectRepository.save(existingProject);
    }

    public Project approveProject(Long id, Boolean isApproved, String rejectionReason) {
        Project project = getProjectById(id);
        if (isApproved) {
            project.setIsApproved(true);
            project.setIsRejected(false);
            project.setRejectionReason(null);
        } else {
            project.setIsApproved(false);
            project.setIsRejected(true);
            project.setRejectionReason(rejectionReason);
        }
        return projectRepository.save(project);
    }

    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}
