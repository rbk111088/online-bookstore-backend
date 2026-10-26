package com.online.bookstore.controller;

import com.online.bookstore.dto.UserLoginRequest;
import com.online.bookstore.dto.UserRegisterRequest;
import com.online.bookstore.model.User;
import com.online.bookstore.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/userAuth")
public class UserAuthController {

    private final UserAuthService userAuthService;
    private  final SecurityContextRepository securityContextRepository;

    public UserAuthController(UserAuthService userAuthService, SecurityContextRepository securityContextRepository) {
        this.userAuthService = userAuthService;
        this.securityContextRepository = securityContextRepository;
    }

    /**
     * Registers a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegisterRequest userRegisterRequest){
        try{
            return ResponseEntity.ok(userAuthService.registerUser(userRegisterRequest));
        }catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Authenticates a user and establishes a login session.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest,
                                       HttpServletResponse httpServletResponse) {
        try {
            Authentication authentication = userAuthService.authentication(userLoginRequest.getUserName(), userLoginRequest.getPassword());
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

            securityContext.setAuthentication(authentication);
            SecurityContextHolder.setContext(securityContext);
            securityContextRepository.saveContext(securityContext, httpServletRequest, httpServletResponse);

            User user = userAuthService.getUserByUserName(authentication.getName());
            return ResponseEntity.ok(
                    Map.of( "userId", user.getId(),
                            "userName", user.getUserName(),
                            "message" , "Login Successful!")
            );
        } catch (AuthenticationException aex) {
            SecurityContextHolder.clearContext();

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Username or Password");
        }
    }
}
