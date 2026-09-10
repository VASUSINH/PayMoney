package com.PayMoney.Service;

import com.PayMoney.DTO.addUserRequestDTO;
import com.PayMoney.DTO.addUserResponseDTO;
import com.PayMoney.DTO.loginRequestDTO;
import com.PayMoney.DTO.loginResponseDTO;
import com.PayMoney.Entity.userEntity;
import com.PayMoney.Mapper.userMapper;
import com.PayMoney.Repository.userRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class userService {
    @Autowired
    private userRepository userRepository;

    @Autowired
    private userMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private jwtService jwtService;


    //ADD User(Register) Service Class Logic.

    public addUserResponseDTO createUser(addUserRequestDTO userRequest) {

        userEntity user = userMapper.toEntity(userRequest);

        user.setRole("USER");

        user.setPassword(
                passwordEncoder.encode(userRequest.getPassword())
        );

        userEntity savedUser = userRepository.save(user);

        return userMapper.toDTO(savedUser);
    }

    // Login user(Authentication) Service Class logic.

    public loginResponseDTO login(loginRequestDTO loginRequest) {

        userEntity user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        boolean passwordMatches = passwordEncoder.matches(
                loginRequest.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new RuntimeException("Invalid email or password");
        }

        //Token is Created after Login.

        String token = jwtService.generateToken(user);


        return new loginResponseDTO(token);
    }

}
