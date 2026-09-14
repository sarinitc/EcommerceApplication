package org.example.ecommerceapplication.customer.service;

import org.example.ecommerceapplication.address.repository.AddressRepository;
import org.example.ecommerceapplication.auth.service.EmailService;
import org.example.ecommerceapplication.customer.controller.AdminCustomerController;
import org.example.ecommerceapplication.customer.exception.CustomerNotFoundException;
import org.example.ecommerceapplication.orders.repository.OrderRepository;
import org.example.ecommerceapplication.profile.service.UploadService;
import org.example.ecommerceapplication.user.entity.*;
import org.example.ecommerceapplication.user.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(AdminCustomerImageTest.Config.class)
class AdminCustomerImageTest {
    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean UserRepository users() { return mock(UserRepository.class); }
        @Bean UploadService uploads(UserRepository users) { return new UploadService(users); }
        @Bean AdminCustomerService customers(UserRepository users, UploadService uploads) {
            return new AdminCustomerService(users, mock(OrderRepository.class), mock(RoleRepository.class),
                    mock(AddressRepository.class), mock(PasswordEncoder.class), mock(EmailService.class), uploads);
        }
    }

    @Autowired UserRepository users;
    @Autowired AdminCustomerService customers;
    private User customer;
    private final MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg",
            new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff});

    @BeforeEach
    void prepare() {
        reset(users);
        customer = User.builder().id(26L).email("customer@example.com")
                .password("unchanged").verified(false).accountStatus(AccountStatus.BLOCKED)
                .roles(new HashSet<>(Set.of(Role.builder().name("CUSTOMER").build()))).build();
        when(users.findById(26L)).thenReturn(Optional.of(customer));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void adminMultipartRequestStoresPhotoForSelectedCustomer() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new AdminCustomerController(customers)).build();
        try {
            mvc.perform(multipart("/api/v1/admin/customers/26/image").file(file))
                    .andExpect(status().isOk());
            assertTrue(customer.getProfileImage().startsWith("http://localhost:8081/uploads/profiles/"));
            assertEquals(AccountStatus.BLOCKED, customer.getAccountStatus());
            assertFalse(customer.isVerified());
            assertEquals("unchanged", customer.getPassword());
            verify(users).save(customer);
            verify(users, never()).findByEmailIgnoreCase(anyString());
        } finally {
            if (customer.getProfileImage() != null) {
                String name = customer.getProfileImage().substring(customer.getProfileImage().lastIndexOf('/') + 1);
                assertTrue(name.matches("[0-9a-f-]{36}\\.jpg"));
                Files.deleteIfExists(Path.of("uploads", "profiles", name));
            }
        }
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotUseAdminUpload() {
        assertThrows(AccessDeniedException.class, () -> customers.uploadCustomerImage(26L, file));
        verifyNoInteractions(users);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectsMissingOrPrivilegedTargetsWithoutSaving() {
        assertThrows(CustomerNotFoundException.class, () -> customers.uploadCustomerImage(999L, file));
        for (String role : new String[]{"ADMIN", "SELLER"}) {
            customer.getRoles().add(Role.builder().name(role).build());
            assertThrows(CustomerNotFoundException.class, () -> customers.uploadCustomerImage(26L, file));
            customer.getRoles().removeIf(r -> role.equals(r.getName()));
        }
        verify(users, never()).save(any());
        assertNull(customer.getProfileImage());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void multipartFieldMustBeNamedFile() throws Exception {
        MockMvcBuilders.standaloneSetup(new AdminCustomerController(customers)).build()
                .perform(multipart("/api/v1/admin/customers/26/image")
                        .file(new MockMultipartFile("profileImage", file.getBytes())))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(users);
    }
}
