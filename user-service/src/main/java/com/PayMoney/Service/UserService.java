
package com.PayMoney.Service;

import com.PayMoney.DTO.*;
import com.PayMoney.Entity.AccountStatus;
import com.PayMoney.Entity.UserEntity;
import com.PayMoney.Exception.UserAlreadyExistsException;
import com.PayMoney.Exception.UserNotFoundException;
import com.PayMoney.Mapper.UserMapper;
import com.PayMoney.Repository.UserRepository;
import com.PayMoney.Producer.UserEventProducer;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
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
    private UserEventProducer userEventProducer;

    @Transactional
    public UserResponseDTO register(UserRequestDTO userRequest) {

        if (userRepository.findByEmailIgnoreCase(userRequest.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        UserEntity user = userMapper.toEntity(userRequest);
        user.setRole("USER");
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        UserEntity savedUser = userRepository.save(user);

        // wallet-service creates the wallet when it receives this event
        userEventProducer.publishUserRegistered(
                new UserRegisteredEventDTO(
                        savedUser.getUserId(),
                        savedUser.getName(),
                        savedUser.getEmail()));

        return userMapper.toDTO(savedUser);
    }

    public LoginResponseDTO login(LoginRequestDTO request) {

        UserEntity user = userRepository
                .findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BadCredentialsException(
                    "Account is not active. Contact support or reactivate your account if eligible.");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponseDTO(token);
    }

    public UserAuthResponseDTO getUserByEmail(String email) {

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        return userMapper.toAuthDTO(user);
    }

    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toDTO);
    }

    public UserResponseDTO getUserById(Long userId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        return userMapper.toDTO(user);
    }

    @Transactional
    public UserResponseDTO updateUser(
            Long userId,
            ProfileUpdateDTO userRequest) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));

        if (!user.getEmail().equalsIgnoreCase(userRequest.getEmail())
                && userRepository.existsByEmailIgnoreCaseAndUserIdNot(
                userRequest.getEmail(), userId)) {
            throw new UserAlreadyExistsException(
                    "Email is already registered");
        }

        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());

        UserEntity updatedUser = userRepository.save(user);

        // wallet-service updates the wallet name when it receives this event
        userEventProducer.publishUserUpdated(
                new UserUpdatedEventDTO(
                        updatedUser.getUserId(),
                        updatedUser.getName()));

        return userMapper.toDTO(updatedUser);
    }


    @Transactional
    public void deactivateMyAccount() {

        UserEntity user = getCurrentAuthenticatedUser();

        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only an active account can be deactivated");
        }

        user.setAccountStatus(AccountStatus.DEACTIVATED);
        userRepository.save(user);
    }


    @Transactional
    public void reactivateMyAccount(ReactivateAccountRequestDTO request) {

        UserEntity user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Account is already active");
        }

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
    }



    private UserEntity getCurrentAuthenticatedUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UserEntity)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user not found");
        }

        UserEntity principal = (UserEntity) authentication.getPrincipal();

        return userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException("User not found"));
    }
}
