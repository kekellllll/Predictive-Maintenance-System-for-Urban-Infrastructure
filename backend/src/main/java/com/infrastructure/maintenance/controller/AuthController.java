package com.infrastructure.maintenance.controller;

import com.infrastructure.maintenance.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        // Simple hardcoded authentication for demo purposes
        // In production, this should check against a real user database
        if (isValidUser(username, password)) {
            String role = getUserRole(username);
            String token = tokenProvider.generateToken(username, role);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", username);
            response.put("role", role);
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> tokenRequest) {
        String token = tokenRequest.get("token");

        if (tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            String role = tokenProvider.getRoleFromToken(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("username", username);
            response.put("role", role);

            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("message", "Invalid token");

            return ResponseEntity.status(401).body(response);
        }
    }

    private boolean isValidUser(String username, String password) {
        // Demo users - in production, check against database
        Map<String, String> users = Map.of(
                "admin", passwordEncoder.encode("admin123"),
                "manager", passwordEncoder.encode("manager123"),
                "operator", passwordEncoder.encode("operator123"),
                "viewer", passwordEncoder.encode("viewer123")
        );

        String encodedPassword = users.get(username);
        return encodedPassword != null && passwordEncoder.matches(password, encodedPassword);
    }

    private String getUserRole(String username) {
        Map<String, String> userRoles = Map.of(
                "admin", "ADMIN",
                "manager", "MANAGER",
                "operator", "OPERATOR",
                "viewer", "VIEWER"
        );

        return userRoles.getOrDefault(username, "VIEWER");
    }
}