package com.example.blog.controller;

import com.example.blog.dto.BlogRequest;
import com.example.blog.dto.BlogResponse;
import com.example.blog.service.BlogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/blogs")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BlogController {
    
    @Autowired
    private BlogService blogService;

    @GetMapping
    public ResponseEntity<List<BlogResponse>> getAllBlogs(Authentication authentication) {
        List<BlogResponse> blogs = blogService.getAllBlogs(authentication.getName());
        return ResponseEntity.ok(blogs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBlogById(@PathVariable Long id, Authentication authentication) {
        try {
            Optional<BlogResponse> blog = blogService.getBlogById(id, authentication.getName());
            if (blog.isPresent()) {
                return ResponseEntity.ok(blog.get());
            } else {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Blog not found!");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getBlogsByUser(@PathVariable String username) {
        try {
            List<BlogResponse> blogs = blogService.getBlogsByUser(username);
            return ResponseEntity.ok(blogs);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping
    public ResponseEntity<?> createBlog(@Valid @RequestBody BlogRequest blogRequest, 
                                       Authentication authentication) {
        try {
            BlogResponse blog = blogService.createBlog(blogRequest, authentication.getName());
            return ResponseEntity.ok(blog);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBlog(@PathVariable Long id,
                                       @Valid @RequestBody BlogRequest blogRequest,
                                       Authentication authentication) {
        try {
            BlogResponse blog = blogService.updateBlog(id, blogRequest, authentication.getName());
            return ResponseEntity.ok(blog);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBlog(@PathVariable Long id, Authentication authentication) {
        try {
            blogService.deleteBlog(id, authentication.getName());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Blog deleted successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}