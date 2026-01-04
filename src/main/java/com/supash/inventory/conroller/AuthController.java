package com.supash.inventory.conroller;

import com.supash.inventory.dto.AuthRequest;
import com.supash.inventory.model.User;
import com.supash.inventory.repo.UserRepository;
import com.supash.inventory.utils.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authManager;
	private final JwtUtil jwtUtil;
	private final UserRepository repo;

	public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, UserRepository repo) {
		this.authManager = authManager;
		this.jwtUtil = jwtUtil;
		this.repo = repo;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody AuthRequest request, HttpServletResponse response) {

		// ✅ 1. Authenticate user
		authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		// ✅ 2. Fetch user & role
		User user = repo.findByUsername(request.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// ✅ 3. Generate JWT
		String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

		// ✅ 4. Create HttpOnly cookie
		ResponseCookie cookie = ResponseCookie.from("jwt", token).httpOnly(true).secure(false) // ⚠️ TRUE in PROD
																								// (HTTPS)
				.path("/").sameSite("Lax").maxAge(24 * 60 * 60).build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		System.out.println("login");
		return ResponseEntity.ok("Login successful");
	}
	
	@GetMapping("/me")
	public ResponseEntity<?> me(Authentication authentication) {
	    if (authentication == null || !authentication.isAuthenticated()) {
	        return ResponseEntity.status(401).body("Not authenticated");
	    }
	    return ResponseEntity.ok(authentication.getPrincipal());
	}

	
	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletResponse response) {
	    ResponseCookie cookie = ResponseCookie.from("jwt", "")
	            .httpOnly(true)
	            .secure(false) // true in prod
	            .path("/")
	            .maxAge(0)
	            .build();

	    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	    return ResponseEntity.ok("Logged out");
	}


}
