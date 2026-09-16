package com.PayMoney.Config;

import com.PayMoney.Entity.userEntity;
import com.PayMoney.Repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class adminInitializer implements CommandLineRunner {

    @Autowired
    private userRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {

        if (userRepository.findByEmail(adminEmail).isEmpty()) {

            userEntity admin = new userEntity();

            admin.setName("PayMoney Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");

            userRepository.save(admin);

            System.out.println("PayMoney ADMIN account created.");
        }
    }
}