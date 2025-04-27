package org.example.seeder;

import lombok.RequiredArgsConstructor;
import org.example.entities.RoleEntity;
import org.example.entities.UserEntity;
import org.example.entities.UserRoleEntity;
import org.example.repository.IRoleRepository;
import org.example.repository.IUserRepository;
import org.example.repository.IUserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSeeder {
    private final IRoleRepository roleRepository;
    private final IUserRepository userRepository;
    private final IUserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public void seed() {
        if(userRepository.count()==0) {
            var admin = new UserEntity();
            admin.setUsername("admin@mail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setGoogleCreated(false);

            admin = userRepository.save(admin);

            RoleEntity adminRole = roleRepository.findByName("ADMIN").orElseThrow();
            UserRoleEntity ur = new UserRoleEntity(null, admin, adminRole);
            userRoleRepository.save(ur);
            System.out.println("---Admin user seeded---");
        }
    }
}
