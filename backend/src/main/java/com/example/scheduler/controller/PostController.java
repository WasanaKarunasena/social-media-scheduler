package com.example.scheduler.controller;

import com.example.scheduler.entity.Post;
import com.example.scheduler.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {

    private final String UPLOAD_DIR = "uploads/";

    @Autowired
    private PostService service;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<Post> create(
            @RequestPart("content") String content,
            @RequestPart("scheduledAt") String scheduledAt,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        Post post = new Post();
        post.setContent(content);
        post.setScheduledAt(LocalDateTime.parse(scheduledAt));
        post.setPublished(false);

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImage(image);
            post.setImageUrl(imageUrl);
        }

        Post saved = service.schedulePost(post);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Post> all() {
        return service.getAllPosts();
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Post> update(
            @PathVariable Long id,
            @RequestPart("content") String content,
            @RequestPart("scheduledAt") String scheduledAt,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        Post updatedPost = new Post();
        updatedPost.setContent(content);
        updatedPost.setScheduledAt(LocalDateTime.parse(scheduledAt));

        if (image != null && !image.isEmpty()) {
            String imageUrl = saveImage(image);
            updatedPost.setImageUrl(imageUrl);
        } else {
            // To keep existing image if no new image sent:
            Post existing = service.getAllPosts().stream()
                    .filter(p -> p.getId().equals(id))
                    .findFirst().orElse(null);
            if (existing != null) {
                updatedPost.setImageUrl(existing.getImageUrl());
            }
        }

        Post saved = service.updatePost(id, updatedPost);
        if (saved == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = service.deletePost(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private String saveImage(MultipartFile file) throws IOException {
        String filenameExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String generatedFileName = UUID.randomUUID().toString() + "." + filenameExtension;
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(generatedFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return the URL or relative path. Adjust based on your front/backend setup.
        return "/uploads/" + generatedFileName;
    }
}
