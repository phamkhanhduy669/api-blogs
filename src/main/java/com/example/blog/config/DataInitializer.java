package com.example.blog.config;

import com.example.blog.entity.Role;
import com.example.blog.entity.Role.RoleName;
import com.example.blog.entity.User;
import com.example.blog.entity.Blog;
import com.example.blog.repository.UserRepository;
import com.example.blog.repository.RoleRepository;
import com.example.blog.repository.BlogRepository;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository, RoleRepository roleRepository, BlogRepository blogRepository, PasswordEncoder encoder) {
        return args -> {
        Role roleAdmin = roleRepository.findByName(RoleName.ROLE_ADMIN)
            .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_ADMIN)));
        Role roleUser = roleRepository.findByName(RoleName.ROLE_USER)
            .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_USER)));

            User admin = userRepository.findByUsername("admin").orElseGet(() -> {
                User u = new User("admin", encoder.encode("admin123"));
                u.addRole(roleAdmin);
                u.addRole(roleUser);
                return userRepository.save(u);
            });
            User user1 = userRepository.findByUsername("user1").orElseGet(() -> {
                User u = new User("user1", encoder.encode("user1123"));
                u.addRole(roleUser);
                return userRepository.save(u);
            });
            User user2 = userRepository.findByUsername("user2").orElseGet(() -> {
                User u = new User("user2", encoder.encode("user2123"));
                u.addRole(roleUser);
                return userRepository.save(u);
            });

            // Sample blogs
            if (blogRepository.findByAuthor(user1).isEmpty()) {
                blogRepository.save(new Blog("Chào mừng từ user1", "Đây là bài viết đầu tiên của user1.", user1));
                blogRepository.save(new Blog("Kinh nghiệm học Spring", "user1 chia sẻ vài ghi chú về Spring Boot.", user1));
            }
            if (blogRepository.findByAuthor(user2).isEmpty()) {
                blogRepository.save(new Blog("Hello từ user2", "Bài viết mở đầu của user2.", user2));
                blogRepository.save(new Blog("Ghi chú JWT", "user2 lưu lại cách hoạt động của JWT trong dự án này.", user2));
            }
            if (blogRepository.findByAuthor(admin).isEmpty()) {
                blogRepository.save(new Blog("Thông báo hệ thống", "Admin: Chào mừng mọi người đến với ứng dụng blog.", admin));
            }

            log.info("==== DB SNAPSHOT (users) ====");
            userRepository.findAll().forEach(u -> {
                long count = blogRepository.findByAuthor(u).size();
                String roles = String.join(",", u.getRoles().stream().map(r->r.getName().name()).toList());
                String pwdPrefix = u.getPassword() != null && u.getPassword().length() > 10 ? u.getPassword().substring(0,10) : u.getPassword();
                log.info("User: {} | roles=[{}] | blogs={} | pwdHashPrefix={}", u.getUsername(), roles, count, pwdPrefix);
            });
            log.info("=============================");
        };
    }
}
