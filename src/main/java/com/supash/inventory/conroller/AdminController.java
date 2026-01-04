package com.supash.inventory.conroller;

import com.supash.inventory.dto.CreateUserRequest;
import com.supash.inventory.model.Role;
import com.supash.inventory.model.User;
import com.supash.inventory.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 🔐 ADMIN ONLY
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole()));

        userRepository.save(user);

        return ResponseEntity.ok("User created successfully");
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateRole(
            @PathVariable String id,
            @RequestParam String role
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(Role.valueOf(role));
        userRepository.save(user);

        return ResponseEntity.ok("Role updated");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok("User deleted");
    }

}
