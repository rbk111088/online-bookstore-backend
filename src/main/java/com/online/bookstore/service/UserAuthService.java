package com.online.bookstore.service;

import com.online.bookstore.dto.UserRegisterRequest;
import com.online.bookstore.model.User;
import com.online.bookstore.repository.UserAuthRepository;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class UserAuthService {

    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public UserAuthService(UserAuthRepository userAuthRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userAuthRepository = userAuthRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public String registerUser(@Valid @RequestBody UserRegisterRequest userRegisterRequest){
        if(userAuthRepository.findByUserName(userRegisterRequest.getUserName()).isPresent()){
            throw new IllegalArgumentException("Username already exists, try with new one");
        }
        String enCodedCredentials = passwordEncoder.encode(userRegisterRequest.getPassword());
        User user = new User(userRegisterRequest.getUserName(),enCodedCredentials);
        userAuthRepository.save(user);

        return "User Registration Successful :) Welcome, " +userRegisterRequest.getUserName() + "!" ;
    }

    public Authentication authentication(String userName, String password){
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
    }

    @Transactional(readOnly = true)
    public User getUserByUserName(String userName){
        return userAuthRepository.findByUserName(userName).orElseThrow(() -> new IllegalStateException("User not Found"));
    }
}
