package com.diplom.levanteak.service.impl;

import com.diplom.levanteak.dto.Response;
import com.diplom.levanteak.exception.InvalidCredentialsException;
import com.diplom.levanteak.exception.NotFoundException;
import com.diplom.levanteak.service.interf.UserService;
import com.diplom.levanteak.dto.LoginRequest;
import com.diplom.levanteak.dto.UserDto;
import com.diplom.levanteak.entity.User;
import com.diplom.levanteak.enums.UserRole;
import com.diplom.levanteak.mapper.EntityDtoMapper;
import com.diplom.levanteak.repository.UserRepo;
import com.diplom.levanteak.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EntityDtoMapper entityDtoMapper;


    @Override
    public Response registerUser(UserDto registrationRequest) {
        log.info("Registering new user with email: {}", registrationRequest.getEmail());

        UserRole role = UserRole.USER;
        if (registrationRequest.getRole() != null && registrationRequest.getRole().equalsIgnoreCase("admin")) {
            role = UserRole.ADMIN;
        }

        User user = User.builder()
                .name(registrationRequest.getName())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .phoneNumber(registrationRequest.getPhoneNumber())
                .role(role)
                .build();

        User savedUser = userRepo.save(user);
        log.info("User successfully registered with ID: {}", savedUser.getId());

        UserDto userDto = entityDtoMapper.mapUserToDtoBasic(savedUser);
        return Response.builder()
                .status(200)
                .message("User Successfully Added")
                .user(userDto)
                .build();
    }



    @Override
    public Response loginUser(LoginRequest loginRequest) {
        log.info("Attempting login for email: {}", loginRequest.getEmail());

        User user = userRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: email not found - {}", loginRequest.getEmail());
                    return new NotFoundException("Email not found");
                });

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed for user {}: invalid password", loginRequest.getEmail());
            throw new InvalidCredentialsException("Password does not match");
        }

        String token = jwtUtils.generateToken(user);
        log.info("Login successful for user ID: {}", user.getId());

        return Response.builder()
                .status(200)
                .message("User Successfully Logged In")
                .token(token)
                .expirationTime("6 Month")
                .role(user.getRole().name())
                .build();
    }

    @Override
    public Response getAllUsers() {
        log.info("Fetching all users from database");

        List<User> users = userRepo.findAll();
        List<UserDto> userDtos = users.stream()
                .map(entityDtoMapper::mapUserToDtoBasic)
                .toList();

        log.debug("Total users found: {}", userDtos.size());

        return Response.builder()
                .status(200)
                .userList(userDtos)
                .build();
    }

    @Override
    public User getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        log.info("Getting currently logged-in user: {}", email);

        return userRepo.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Logged-in user not found with email: {}", email);
                    return new UsernameNotFoundException("User Not found");
                });
    }


    @Override
    public Response getUserInfoAndOrderHistory() {
        log.info("Fetching logged-in user info and order history");
        User user = getLoginUser();
        UserDto userDto = entityDtoMapper.mapUserToDtoPlusAddressAndOrderHistory(user);
        log.debug("User info loaded for ID: {}", user.getId());

        return Response.builder()
                .status(200)
                .user(userDto)
                .build();
    }
}
