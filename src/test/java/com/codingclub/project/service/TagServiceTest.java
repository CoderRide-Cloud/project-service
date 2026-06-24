package com.codingclub.project.service;

import com.codingclub.project.model.Tag;
import com.codingclub.project.repository.TagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    void testSearchTags_ReturnsEmptyForBlankQuery() {
        assertTrue(tagService.searchTags("").isEmpty());
        assertTrue(tagService.searchTags(null).isEmpty());
    }

    @Test
    void testSearchTags_StripsHashPrefix() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("java");

        when(tagRepository.findByNameStartingWithIgnoreCase(eq("java"), any(Pageable.class)))
                .thenReturn(List.of(tag));

        List<Tag> results = tagService.searchTags("#java");

        assertEquals(1, results.size());
        assertEquals("java", results.get(0).getName());
    }

    @Test
    void testCreateTag_RejectsDuplicate() {
        Tag existing = new Tag();
        existing.setId(1L);
        existing.setName("spring");

        when(tagRepository.findByName("spring")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> tagService.createTag("spring"));
    }
}
