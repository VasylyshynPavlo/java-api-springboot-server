package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.config.security.JwtService;
import org.example.dto.user.UserAuthDto;
import org.example.dto.user.UserRegisterDto;
import org.example.entities.UserEntity;
import org.example.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public void registerUser(UserRegisterDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Користувач з таким ім'ям вже існує");
        }
        var userEntity = new UserEntity();
        userEntity.setUsername(dto.getUsername());
        userEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
        userRepository.save(userEntity);
    }

    public String authenticateUser(UserAuthDto userEntity) {
        UserEntity foundUser = userRepository.findByUsername(userEntity.getUsername())
                .orElseThrow(() -> new RuntimeException("Невірно вказано дані"));

        if (!passwordEncoder.matches(userEntity.getPassword(), foundUser.getPassword())) {
            throw new RuntimeException("Невірно вказано дані");
        }

        // Генерація JWT токену
        return jwtService.generateAccessToken(foundUser);
    }
}