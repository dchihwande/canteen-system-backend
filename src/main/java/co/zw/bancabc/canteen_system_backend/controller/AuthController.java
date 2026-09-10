package co.zw.bancabc.canteen_system_backend.controller;

import co.zw.bancabc.canteen_system_backend.dto.ApiResponse;
import co.zw.bancabc.canteen_system_backend.dto.LoginRequest;
import co.zw.bancabc.canteen_system_backend.repository.UserRepository;
import co.zw.bancabc.canteen_system_backend.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword()));

            boolean isCashier = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equals("ROLE_CASHIER"));

            String token = jwtUtil.generateToken(auth.getName(), isCashier);

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", auth.getName());
            data.put("isCashier", isCashier);

            // Best-effort email lookup
            userRepository.findByUsername(auth.getName())
                    .ifPresent(u -> data.put("email", u.getEmail()));

            return ResponseEntity.ok(ApiResponse.success("Login successful", data));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid credentials"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
        }
        boolean isCashier = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_CASHIER"));

        Map<String, Object> data = new HashMap<>();
        data.put("username", auth.getName());
        data.put("isCashier", isCashier);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}