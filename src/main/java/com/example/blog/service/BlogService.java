package com.example.blog.service;

import com.example.blog.dto.BlogRequest;
import com.example.blog.dto.BlogResponse;
import com.example.blog.entity.Blog;
import com.example.blog.entity.Role;
import com.example.blog.entity.User;
import com.example.blog.repository.BlogRepository;
import com.example.blog.repository.UserRepository;
import com.example.blog.dto.UserSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlogService {
    
    @Autowired
    private BlogRepository blogRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<BlogResponse> getAllBlogs(String currentUsername) {
        User current = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Blog> blogs;
    boolean isAdmin = current.getRoles().stream().anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN);
    if (isAdmin) {
            blogs = blogRepository.findAllByOrderByCreatedAtDesc();
        } else {
            blogs = blogRepository.findByAuthorOrderByCreatedAtDesc(current);
        }
        return blogs.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    public List<BlogResponse> getBlogsByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return blogRepository.findByAuthorOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Optional<BlogResponse> getBlogById(Long id, String currentUsername) {
        Optional<Blog> blog = blogRepository.findById(id);
        if (blog.isPresent()) {
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Blog blogEntity = blog.get();
            if (!isOwnerOrAdmin(blogEntity, currentUser)) {
                throw new AccessDeniedException("You can only view your own blogs");
            }
            return Optional.of(convertToResponse(blogEntity));
        }
        return Optional.empty();
    }

    public BlogResponse createBlog(BlogRequest request, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Blog blog = new Blog(request.getTitle(), request.getContent(), author);
        Blog savedBlog = blogRepository.save(blog);
        return convertToResponse(savedBlog);
    }

    public BlogResponse updateBlog(Long id, BlogRequest request, String username) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!isOwnerOrAdmin(blog, currentUser)) {
            throw new AccessDeniedException("You can only update your own blogs");
        }
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        Blog updatedBlog = blogRepository.save(blog);
        return convertToResponse(updatedBlog);
    }

    public void deleteBlog(Long id, String username) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!isOwnerOrAdmin(blog, currentUser)) {
            throw new AccessDeniedException("You can only delete your own blogs");
        }
        blogRepository.delete(blog);
    }

    public void deleteUser(String targetUsername, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        // Only admin can delete users
    boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN);
    if (!isAdmin) {
            throw new AccessDeniedException("Only admin can delete users");
        }
        
        User targetUser = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found"));
    // Prevent admin from deleting self or another admin account
    if (targetUser.getUsername().equals(currentUsername)) {
        throw new AccessDeniedException("Admin cannot delete their own account");
    }
    boolean targetIsAdmin = targetUser.getRoles().stream().anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN);
    if (targetIsAdmin) {
        throw new AccessDeniedException("Cannot delete another admin");
    }
        
        userRepository.delete(targetUser);
    }

    public List<UserSummary> getAllUsers(String currentUsername) {
    User currentUser = userRepository.findByUsername(currentUsername)
        .orElseThrow(() -> new RuntimeException("Current user not found"));
    boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN);
    if(!isAdmin) throw new AccessDeniedException("Only admin can list users");

    return userRepository.findAll().stream()
        .map(u -> new UserSummary(
            u.getUsername(),
            u.getRoles().stream().map(r->r.getName().name()).toList(),
            blogRepository.findByAuthor(u).size()
        ))
        .collect(Collectors.toList());
    }

    private boolean isOwnerOrAdmin(Blog blog, User currentUser) {
    boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN);
    return blog.getAuthor().getId().equals(currentUser.getId()) || isAdmin;
    }

    private BlogResponse convertToResponse(Blog blog) {
        return new BlogResponse(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getAuthor().getUsername(),
                blog.getCreatedAt(),
                blog.getUpdatedAt()
        );
    }
}