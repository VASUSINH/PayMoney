package com.PayMoney.service;

import com.PayMoney.DTO.LoginRequestDTO;
import com.PayMoney.DTO.UserRequestDTO;
import com.PayMoney.Entity.AccountStatus;
import com.PayMoney.Entity.UserEntity;
import com.PayMoney.Exception.UserAlreadyExistsException;
import com.PayMoney.Mapper.UserMapper;
import com.PayMoney.Producer.UserEventProducer;
import com.PayMoney.Repository.UserRepository;
import com.PayMoney.Service.JwtService;
import com.PayMoney.Service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserEventProducer userEventProducer;

    @Test
    void register_shouldThrowException_whenEmailAlreadyRegistered() {
        UserRequestDTO request = new UserRequestDTO();
        request.setName("Alice");
        request.setEmail("alice@test.com");
        request.setPassword("Password@123");

        when(userRepository.findByEmailIgnoreCase("alice@test.com"))
                .thenReturn(Optional.of(new UserEntity()));

        assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.register(request)
        );

        verify(userRepository, never()).save(any(UserEntity.class));
        verifyNoInteractions(userEventProducer);
    }

    @Test
    void login_shouldThrowException_whenPasswordIsWrong() {
        UserEntity user = new UserEntity();
        user.setEmail("alice@test.com");
        user.setPassword("hashed-password");
        user.setAccountStatus(AccountStatus.ACTIVE);

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("alice@test.com");
        request.setPassword("wrong-password");

        when(userRepository.findByEmailIgnoreCase("alice@test.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password"))
                .thenReturn(false);

        assertThrows(
                BadCredentialsException.class,
                () -> userService.login(request)
        );

        verifyNoInteractions(jwtService);
    }
}
