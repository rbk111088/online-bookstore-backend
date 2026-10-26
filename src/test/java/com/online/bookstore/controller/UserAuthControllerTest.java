package com.online.bookstore.controller;

import com.online.bookstore.dto.UserLoginRequest;
import com.online.bookstore.dto.UserRegisterRequest;
import com.online.bookstore.model.User;
import com.online.bookstore.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthControllerTest {
    @Mock
    private UserAuthService userAuthService;
    private SecurityContextRepository contextRepository;
    @InjectMocks
    private UserAuthController controller;

    @BeforeEach
    void setUp() {
        userAuthService = mock(UserAuthService.class);
        contextRepository = mock(SecurityContextRepository.class);

        controller = new UserAuthController(userAuthService, contextRepository);
        SecurityContextHolder.clearContext();
    }

    @Test
    void registersNewUser() {
        UserRegisterRequest request =
                new UserRegisterRequest("reader", "secret1");

        when(userAuthService.registerUser(request))
                .thenReturn("User Registration Successful :) Welcome, reader!");

        ResponseEntity<?> response = controller.registerUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(
                "User Registration Successful :) Welcome, reader!",
                response.getBody());

        verify(userAuthService).registerUser(request);
    }

    @Test
    void rejectsDuplicateUsername() {
        UserRegisterRequest request =
                new UserRegisterRequest("reader", "secret1");

        when(userAuthService.registerUser(request))
                .thenThrow(new IllegalArgumentException(
                        "Username already exists, try with new one"));

        ResponseEntity<?> response = controller.registerUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(
                "Username already exists, try with new one",
                response.getBody());
    }

    @Test
    void rejectsInvalidLoginCredentials() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUserName("reader");
        request.setPassword("wrong");

        when(userAuthService.authentication("reader", "wrong"))
                .thenThrow(new BadCredentialsException("Invalid"));

        ResponseEntity<?> response = controller.loginUser(
                request,
                mock(HttpServletRequest.class),
                mock(HttpServletResponse.class));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());

        verify(contextRepository, never())
                .saveContext(any(), any(), any());
    }

    @Test
    void savesSecurityContextOnSuccessfulLogin() {
        UserLoginRequest request = new UserLoginRequest();
        request.setUserName("reader");
        request.setPassword("secret1");

        Authentication authenticated = mock(Authentication.class);
        when(authenticated.getName()).thenReturn("reader");

        User user = new User();
        user.setId(1L);
        user.setUserName("reader");

        when(userAuthService.authentication("reader", "secret1"))
                .thenReturn(authenticated);
        when(userAuthService.getUserByUserName("reader"))
                .thenReturn(user);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);

        ResponseEntity<?> response = controller.loginUser(
                request, httpRequest, httpResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertNotNull(body);
        assertEquals(1L, body.get("userId"));
        assertEquals("reader", body.get("userName"));
        assertEquals("Login Successful!", body.get("message"));

        verify(contextRepository)
                .saveContext(any(), eq(httpRequest), eq(httpResponse));
    }
}
