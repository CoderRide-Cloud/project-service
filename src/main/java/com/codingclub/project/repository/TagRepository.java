package com.codingclub.project.repository;

import com.codingclub.project.model.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByName(String name);
    List<Tag> findByNameStartingWithIgnoreCase(String name, Pageable pageable);
    List<Tag> findAllByOrderByNameAsc(Pageable pageable);
}
