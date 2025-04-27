package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.config.security.JwtService;
import org.example.dto.user.UserAuthDto;
import org.example.dto.user.UserGetDto;
import org.example.dto.user.UserRegisterDto;
import org.example.entities.UserEntity;
import org.example.entities.UserRoleEntity;
import org.example.mapper.UserMapper;
import org.example.repository.IRoleRepository;
import org.example.repository.IUserRepository;
import org.example.repository.IUserRoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final IRoleRepository roleRepository;
    private final IUserRoleRepository userRoleRepository;
    private final FileService fileService;

    @Value("${google.api.userinfo}")
    private String googleUserInfoUrl;

    public UserGetDto getUserDto(String username)
    {
        if (!userRepository.existsByUsername(username)) {
            throw new RuntimeException("User do not exist");
        }
        UserEntity foundUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Unknown username"));
        return userMapper.toDto(foundUser);
    }

    public void registerUser(UserRegisterDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("User already exist");
        }
        var userEntity = new UserEntity();
        userEntity.setUsername(dto.getUsername());
        userEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
        userEntity.setGoogleCreated(false);
        var newImageFile = dto.getAvatar();
        if (newImageFile != null && !newImageFile.isEmpty()) {
            var imagePath = fileService.load(newImageFile);
            userEntity.setAvatar(imagePath);
        }
        else {
            userEntity.setAvatar("default.png");
        }
        userRepository.save(userEntity);
        var userRoleEntity = new UserRoleEntity();
        userRoleEntity.setUser(userEntity);
        userRoleEntity.setRole(roleRepository.findByName("ADMIN")
                .orElseThrow(() -> new RuntimeException("Role not found")));
        userRoleRepository.save(userRoleEntity);

    }

    public String authenticateUser(UserAuthDto userEntity) {
        UserEntity foundUser = userRepository.findByUsername(userEntity.getUsername())
                .orElseThrow(() -> new RuntimeException("Unknown data"));

        if (!passwordEncoder.matches(userEntity.getPassword(), foundUser.getPassword())) {
            throw new RuntimeException("Unknown data");
        }

        // Генерація JWT токену
        return jwtService.generateAccessToken(foundUser);
    }

    public String signInGoogle(String accessToken) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                googleUserInfoUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to verify Google token");
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> userInfo = mapper.readValue(
                response.getBody(),
                new TypeReference<Map<String, String>>() {}
        );

        String email = userInfo.get("email");
        String pictureUrl = userInfo.get("picture");

        String finalUsername = email;
        int suffix = 1;
        while (userRepository.existsByUsername(finalUsername)) {
            finalUsername = email + suffix++;
        }

        String finalUsername1 = finalUsername;
        String finalUsername2 = finalUsername;
        UserEntity userEntity = userRepository.findByUsername(email)
                .map(existingUser -> {
                    if (existingUser.getUsername() == null || "немає".equals(existingUser.getUsername())) {
                        existingUser.setUsername(finalUsername1);
                        return userRepository.save(existingUser);
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setUsername(finalUsername2);
                    newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    newUser.setGoogleCreated(true);
                    return userRepository.save(newUser);
                });

        if (pictureUrl != null && !pictureUrl.isEmpty()) {
            byte[] newImageFile = restTemplate.getForObject(pictureUrl, byte[].class);
            if (newImageFile != null && newImageFile.length > 0) {
                MultipartFile multipartFile = new MockMultipartFile("avatar", "avatar.jpg", "image/jpeg", newImageFile);
                var imagePath = fileService.load(multipartFile);
                userEntity.setAvatar(imagePath);
            }
        } else {
            userEntity.setAvatar("default.png");
        }

        userRepository.save(userEntity);

        return jwtService.generateAccessToken(userEntity);
    }
}