package com.example.scheduler.service;

import com.example.scheduler.entity.Post;
import com.example.scheduler.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostService {
    @Autowired
    private PostRepository repo;

    public Post schedulePost(Post post) {
        post.setPublished(false);
        return repo.save(post);
    }

    public List<Post> getAllPosts() {
        return repo.findAll();
    }

    public Post updatePost(Long id, Post updatedPost) {
        return repo.findById(id).map(post -> {
            post.setContent(updatedPost.getContent());
            post.setScheduledAt(updatedPost.getScheduledAt());
            post.setImageUrl(updatedPost.getImageUrl());
            return repo.save(post);
        }).orElse(null);
    }

    public boolean deletePost(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    @Scheduled(fixedRate = 60000)
    public void publishScheduledPosts() {
        List<Post> posts = repo.findByPublishedFalseAndScheduledAtBefore(LocalDateTime.now());
        for (Post p : posts) {
            System.out.println("Publishing post: " + p.getContent());
            p.setPublished(true);
            p.setPublishedAt(LocalDateTime.now());
            repo.save(p);
        }
    }
}
