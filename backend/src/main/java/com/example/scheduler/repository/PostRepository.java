package com.example.scheduler.repository;

import com.example.scheduler.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByPublishedFalseAndScheduledAtBefore(LocalDateTime time);
}
