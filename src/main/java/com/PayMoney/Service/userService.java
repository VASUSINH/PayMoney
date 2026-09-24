package com.PayMoney.Service;

import com.PayMoney.DTO.userRequestDTO;
import com.PayMoney.DTO.userResponseDTO;
import com.PayMoney.DTO.loginRequestDTO;
import com.PayMoney.DTO.loginResponseDTO;
import com.PayMoney.Entity.userEntity;
import com.PayMoney.Entity.walletEntity;
import com.PayMoney.Exception.userNotFoundException;
import com.PayMoney.Mapper.userMapper;
import com.PayMoney.Repository.userRepository;
import com.PayMoney.Repository.walletRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.List;


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

    @Autowired
    private walletRepository walletRepo;


    //ADD User(Register) Service Class Logic.

    @Transactional
    public userResponseDTO createUser(userRequestDTO userRequest) {

        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        userEntity user = userMapper.toEntity(userRequest);

        user.setRole("USER");

        user.setPassword(
                passwordEncoder.encode(userRequest.getPassword())
        );

        userEntity savedUser = userRepository.save(user);

        // Wallet creation for the User Added.
        walletEntity wallet = new walletEntity();

        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUser(user);

        walletRepo.save(wallet);

        return userMapper.toDTO(savedUser);
    }

    // Login user(Authentication) Service Class logic.

    public loginResponseDTO login(loginRequestDTO request) {

        userEntity user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not registered"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Password does not match");
        }

        String token = jwtService.generateToken(user);

        return new loginResponseDTO(token);
    }

    public List<userResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }
    public userResponseDTO getUserById(Long userId) {

        userEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userMapper.toDTO(user);
    }

    public userResponseDTO updateUser(Long userId, userRequestDTO userRequest) {

        userEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());

        userEntity updatedUser = userRepository.save(user);

        return userMapper.toDTO(updatedUser);
    }

    public void deleteUser(Long userId) {

        userEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
    }
}
