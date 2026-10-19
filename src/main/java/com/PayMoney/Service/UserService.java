package com.PayMoney.Service;

import com.PayMoney.DTO.*;
import com.PayMoney.Entity.UserEntity;
import com.PayMoney.Entity.WalletEntity;
import com.PayMoney.Exception.UserAlreadyExistsException;
import com.PayMoney.Mapper.UserMapper;
import com.PayMoney.Repository.UserRepository;
import com.PayMoney.Repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.PayMoney.Exception.UserNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;


import java.math.BigDecimal;
import java.util.List;


@Service
@AllArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private WalletRepository walletRepo;



    @Transactional
    public UserResponseDTO register(UserRequestDTO userRequest) {

        if (userRepository.findByEmail(userRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        UserEntity user = userMapper.toEntity(userRequest);

        user.setRole("USER");

        user.setPassword(
                passwordEncoder.encode(userRequest.getPassword())
        );

        UserEntity savedUser = userRepository.save(user);

        WalletEntity wallet = new WalletEntity();

        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUser(user);

        walletRepo.save(wallet);

        return userMapper.toDTO(savedUser);
    }



    public LoginResponseDTO login(LoginRequestDTO request) {

        UserEntity user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return new LoginResponseDTO(token);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }
    public UserResponseDTO getUserById(Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
        return userMapper.toDTO(user);
    }


    public UserResponseDTO updateUser(
            Long userId,
            ProfileUpdateDTO userRequest) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(userRequest.getEmail())
                && userRepository.existsByEmailAndUserIdNot(
                userRequest.getEmail(), userId)) {

            throw new UserAlreadyExistsException(
                    "Email is already registered");
        }

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());

        UserEntity updatedUser = userRepository.save(user);

        return userMapper.toDTO(updatedUser);
    }


    public void deleteUser(Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
        userRepository.delete(user);
    }
}
