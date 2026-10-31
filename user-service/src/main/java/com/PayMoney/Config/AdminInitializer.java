package com.PayMoney.Config;

import com.PayMoney.Entity.UserEntity;
import com.PayMoney.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger log =
            LoggerFactory.getLogger(AdminInitializer.class);


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {


        if (userRepository.findByEmail(adminEmail).isEmpty()) {

            UserEntity admin = new UserEntity();

            admin.setName("PayMoney Admin");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ADMIN");

            userRepository.save(admin);

            log.info("PayMoney ADMIN account created.");
        }
    }
}