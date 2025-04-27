package org.example;

import org.example.seeder.RoleSeeder;
import org.example.seeder.UserSeeder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

//TIP Spring boot
@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

//    @Bean
//    public CommandLineRunner run(RoleSeeder roleSeeder, UserSeeder userSeeder) {
//        return args -> {
//            roleSeeder.seed();
//            userSeeder.seed();
//        };
//    }
}