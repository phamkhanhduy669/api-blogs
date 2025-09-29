package com.example.blog.repository;

import com.example.blog.entity.Blog;
import com.example.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
    List<Blog> findByAuthor(User author);
    List<Blog> findByTitleContainingIgnoreCase(String title);
    List<Blog> findByAuthorOrderByCreatedAtDesc(User author);
    List<Blog> findAllByOrderByCreatedAtDesc();
}