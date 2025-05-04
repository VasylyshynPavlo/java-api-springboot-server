package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.config.security.JwtService;
import org.example.dto.user.UserAuthDto;
import org.example.dto.user.UserGetDto;
import org.example.dto.user.UserGoogleAuthDto;
import org.example.dto.user.UserRegisterDto;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping(path = "/register", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@Valid @ModelAttribute UserRegisterDto dto) {
        try {
            String token = userService.registerUser(dto);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserAuthDto dto) {
        try {
            String token = userService.authenticateUser(dto);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Error with login: " + e.getMessage()));
        }
    }

    @PostMapping("/google")
    public ResponseEntity<Map<String, String>> googleLogin(@Valid @RequestBody UserGoogleAuthDto dto) {
        try {
            String token = userService.signInGoogle(dto.getToken());
            return ResponseEntity.ok(Map.of("Bearer", token));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<UserGetDto> getUserInfo(@Valid @RequestParam("username") String username) {
        try {
            return ResponseEntity.ok(userService.getUserDto(username));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}