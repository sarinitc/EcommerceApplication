package org.example.ecommerceapplication.auth;

import org.example.ecommerceapplication.auth.controller.AuthController;
import org.example.ecommerceapplication.auth.service.AuthService;
import org.example.ecommerceapplication.auth.service.EmailService;
import org.example.ecommerceapplication.auth.service.OtpService;
import org.example.ecommerceapplication.auth.service.PasswordResetOtpService;
import org.example.ecommerceapplication.common.exception.GlobalExceptionhandler;
import org.example.ecommerceapplication.config.SecurityConfig;
import org.example.ecommerceapplication.security.CustomUserDetailsService;
import org.example.ecommerceapplication.security.JwtAccessDeniedHandler;
import org.example.ecommerceapplication.security.JwtAuthenticationFilter;
import org.example.ecommerceapplication.security.JwtService;
import org.example.ecommerceapplication.user.entity.AccountStatus;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.RoleRepository;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@SpringJUnitConfig(ChangePasswordEndpointTest.Config.class)
class ChangePasswordEndpointTest {
    private static final String ENDPOINT = "/api/v1/auth/change-password";
    private static final String CURRENT_PASSWORD = "current-password";
    private static final String NEW_PASSWORD = "new-password";

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, AuthController.class, AuthService.class,
            GlobalExceptionhandler.class, JwtService.class, JwtAuthenticationFilter.class,
            CustomUserDetailsService.class, JwtAccessDeniedHandler.class})
    static class Config {
        @Bean UserRepository users() { return mock(UserRepository.class); }
        @Bean RoleRepository roles() { return mock(RoleRepository.class); }
        @Bean OtpService otpService() { return mock(OtpService.class); }
        @Bean EmailService emailService() { return mock(EmailService.class); }
        @Bean PasswordResetOtpService passwordResetOtpService() {
            return mock(PasswordResetOtpService.class);
        }
    }

    @Autowired WebApplicationContext context;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;
    private MockMvc mvc;
    private User account;
    private String originalHash;
    private String token;

    @BeforeEach
    void prepare() {
        reset(users);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        originalHash = passwordEncoder.encode(CURRENT_PASSWORD);
        account = User.builder().id(42L).username("customer").email("customer@example.com")
                .password(originalHash).verified(true).build();
        when(users.findByEmailIgnoreCase("customer@example.com")).thenReturn(Optional.of(account));
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("customer@example.com").password(originalHash)
                .authorities("ROLE_CUSTOMER").build();
        token = jwtService.generateToken(principal);
    }

    @Test
    void changesAuthenticatedUsersPasswordAndStoresOnlyTheHash() throws Exception {
        mvc.perform(authenticatedRequest(body(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Password changed successfully"))
                .andExpect(jsonPath("$.payload").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(content().string(not(containsString(CURRENT_PASSWORD))))
                .andExpect(content().string(not(containsString(NEW_PASSWORD))));

        verify(users).save(account);
        assertNotEquals(NEW_PASSWORD, account.getPassword());
        assertNotEquals(originalHash, account.getPassword());
        assertTrue(passwordEncoder.matches(NEW_PASSWORD, account.getPassword()));
        assertFalse(passwordEncoder.matches(CURRENT_PASSWORD, account.getPassword()));
    }

    @Test
    void rejectsIncorrectCurrentPasswordWithoutChangingTheAccount() throws Exception {
        mvc.perform(authenticatedRequest(body("wrong-password", NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));

        assertPasswordUnchanged();
    }

    @Test
    void rejectsMismatchedConfirmationWithoutChangingTheAccount() throws Exception {
        mvc.perform(authenticatedRequest(body(CURRENT_PASSWORD, NEW_PASSWORD, "different-password")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.status").value(400));

        assertPasswordUnchanged();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{}",
            "{\"newPassword\":\"new-password\",\"confirmPassword\":\"new-password\"}",
            "{\"currentPassword\":\"current-password\",\"confirmPassword\":\"new-password\"}",
            "{\"currentPassword\":\"current-password\",\"newPassword\":\"new-password\"}",
            "{\"currentPassword\":null,\"newPassword\":\"new-password\",\"confirmPassword\":\"new-password\"}",
            "{\"currentPassword\":\"\",\"newPassword\":\"new-password\",\"confirmPassword\":\"new-password\"}",
            "{\"currentPassword\":\"   \",\"newPassword\":\"new-password\",\"confirmPassword\":\"new-password\"}",
            "{\"currentPassword\":\"current-password\",\"newPassword\":\"        \",\"confirmPassword\":\"        \"}",
            "{\"currentPassword\":\"current-password\",\"newPassword\":\"short77\",\"confirmPassword\":\"short77\"}",
            "{\"currentPassword\":\"current-password\",\"newPassword\":\"new-password\",\"confirmPassword\":\"   \"}"
    })
    void rejectsInvalidFieldsWithoutChangingTheAccount(String requestBody) throws Exception {
        mvc.perform(authenticatedRequest(requestBody)).andExpect(status().isBadRequest());

        assertPasswordUnchanged();
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345678", "  new-password  "})
    void acceptsMinimumLengthAndPreservesPasswordWhitespace(String newPassword) throws Exception {
        mvc.perform(authenticatedRequest(body(CURRENT_PASSWORD, newPassword, newPassword)))
                .andExpect(status().isOk());

        verify(users).save(account);
        assertTrue(passwordEncoder.matches(newPassword, account.getPassword()));
    }

    @Test
    void rejectsNewPasswordsExceedingBcryptByteLimit() throws Exception {
        for (String oversizedPassword : new String[]{"a".repeat(73), "\u20ac".repeat(25)}) {
            mvc.perform(authenticatedRequest(body(CURRENT_PASSWORD, oversizedPassword, oversizedPassword)))
                    .andExpect(status().isBadRequest());
        }

        assertPasswordUnchanged();
    }

    @Test
    void rejectsCurrentPasswordsExceedingBcryptByteLimit() throws Exception {
        for (String oversizedPassword : new String[]{"a".repeat(73), "\u20ac".repeat(25)}) {
            mvc.perform(authenticatedRequest(body(oversizedPassword, NEW_PASSWORD, NEW_PASSWORD)))
                    .andExpect(status().isBadRequest());
        }

        assertPasswordUnchanged();
    }

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(post(ENDPOINT).contentType(MediaType.APPLICATION_JSON)
                        .content(body(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(users);
        assertEquals(originalHash, account.getPassword());
    }

    @Test
    void rejectsInvalidBearerToken() throws Exception {
        mvc.perform(post(ENDPOINT).header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(users);
        assertEquals(originalHash, account.getPassword());
    }

    @Test
    void rejectsBlockedAccountEvenWithAValidToken() throws Exception {
        account.setAccountStatus(AccountStatus.BLOCKED);

        mvc.perform(authenticatedRequest(body(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isForbidden());

        assertPasswordUnchanged();
    }

    @Test
    void rejectsAnAuthenticatedUserThatNoLongerExists() throws Exception {
        mvc.perform(post(ENDPOINT).with(user("missing@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body(CURRENT_PASSWORD, NEW_PASSWORD, NEW_PASSWORD)))
                .andExpect(status().isNotFound());

        assertPasswordUnchanged();
    }

    private MockHttpServletRequestBuilder authenticatedRequest(String requestBody) {
        return post(ENDPOINT).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(requestBody);
    }

    private String body(String currentPassword, String newPassword, String confirmPassword) {
        return """
                {"currentPassword":"%s","newPassword":"%s","confirmPassword":"%s"}
                """.formatted(currentPassword, newPassword, confirmPassword);
    }

    private void assertPasswordUnchanged() {
        verify(users, never()).save(any(User.class));
        assertEquals(originalHash, account.getPassword());
    }
}
