package com.codingclub.project.service;

import com.codingclub.common.exception.ResourceNotFoundException;
import com.codingclub.project.model.Tag;
import com.codingclub.project.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public List<Tag> getAllTags(int skip, int limit) {
        return tagRepository.findAllByOrderByNameAsc(PageRequest.of(skip / Math.max(limit, 1), Math.max(limit, 1)));
    }

    public List<Tag> searchTags(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        String cleanedQuery = query.startsWith("#") ? query.substring(1) : query;
        return tagRepository.findByNameStartingWithIgnoreCase(cleanedQuery, PageRequest.of(0, 10));
    }

    public Tag createTag(String name) {
        if (tagRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Tag already exists");
        }

        Tag tag = new Tag();
        tag.setName(name);
        return tagRepository.save(tag);
    }

    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag not found with ID: " + id);
        }
        tagRepository.deleteById(id);
    }
}
