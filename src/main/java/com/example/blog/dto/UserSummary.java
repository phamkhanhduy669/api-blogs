package com.example.blog.dto;

import java.util.List;

public class UserSummary {
    private String username;
    private List<String> roles;
    private long blogCount;

    public UserSummary(String username, List<String> roles, long blogCount) {
        this.username = username;
        this.roles = roles;
        this.blogCount = blogCount;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public long getBlogCount() { return blogCount; }
    public void setBlogCount(long blogCount) { this.blogCount = blogCount; }
}
