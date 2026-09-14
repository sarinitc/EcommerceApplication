package org.example.ecommerceapplication.security;

import org.example.ecommerceapplication.user.entity.AccountStatus;
import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.FilterChain;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BlockedAccountTest {
    @Test
    void existingTokenIsRejectedAfterBlockAndAcceptedAfterUnblock() throws Exception {
        UserRepository repository = mock(UserRepository.class);
        User user = User.builder().email("customer@example.com").password("hash").build();
        when(repository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        CustomUserDetailsService details = new CustomUserDetailsService(repository);
        JwtService jwt = mock(JwtService.class);
        when(jwt.extractUsername("existing-token")).thenReturn(user.getEmail());
        when(jwt.isTokenValid(eq("existing-token"), any())).thenReturn(true);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwt, details);
        try {
            for (AccountStatus status : new AccountStatus[]{AccountStatus.ACTIVE, AccountStatus.BLOCKED, AccountStatus.ACTIVE}) {
                SecurityContextHolder.clearContext();
                user.setAccountStatus(status);
                MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/me");
                request.setServletPath("/api/v1/auth/me");
                request.addHeader("Authorization", "Bearer existing-token");
                MockHttpServletResponse response = new MockHttpServletResponse();
                FilterChain chain = mock(FilterChain.class);
                filter.doFilter(request, response, chain);
                if (status == AccountStatus.BLOCKED) {
                    assertEquals(403, response.getStatus());
                    assertNull(SecurityContextHolder.getContext().getAuthentication());
                    verifyNoInteractions(chain);
                } else {
                    assertNotNull(SecurityContextHolder.getContext().getAuthentication());
                    verify(chain).doFilter(request, response);
                }
            }
        } finally { SecurityContextHolder.clearContext(); }
    }
}
