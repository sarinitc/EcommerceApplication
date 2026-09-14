package org.example.ecommerceapplication.profile.service;

import org.example.ecommerceapplication.user.entity.User;
import org.example.ecommerceapplication.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UploadServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final UploadService uploads = new UploadService(users);
    private final User user = User.builder().email("customer@example.com").build();

    private void prepare() {
        when(users.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void acceptsSupportedSignaturesAndIgnoresDangerousOriginalFilename() throws Exception {
        prepare();
        String[] types = {"image/jpeg", "image/png", "image/webp"};
        String[] extensions = {"jpg", "png", "webp"};
        byte[][] headers = {
                {(byte) 0xff, (byte) 0xd8, (byte) 0xff},
                {(byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a},
                {'R', 'I', 'F', 'F', 4, 0, 0, 0, 'W', 'E', 'B', 'P'}
        };
        ReflectionTestUtils.setField(uploads, "publicBaseUrl", "https://api.example.com/");
        for (int i = 0; i < types.length; i++) {
            var file = new MockMultipartFile("file", "../../evil.html", types[i], headers[i]);
            var response = uploads.uploadProfileImage(file, user.getEmail());
            Path stored = Path.of("uploads", "profiles", response.getFileName());
            try {
                assertTrue(response.getFileName().matches("[0-9a-f-]{36}\\." + extensions[i]));
                assertEquals("https://api.example.com/uploads/profiles/" + response.getFileName(),
                        response.getProfileImage());
                assertEquals(response.getProfileImage(), user.getProfileImage());
                assertArrayEquals(headers[i], Files.readAllBytes(stored));
            } finally {
                // Delete only the UUID file created by this test.
                Files.deleteIfExists(stored);
            }
        }
    }

    @Test
    void rejectsEmptyOversizeUnsupportedAndSpoofedFilesWithoutSavingUser() {
        prepare();
        var empty = new MockMultipartFile("file", new byte[0]);
        var oversized = new MockMultipartFile("file", "large.jpg", "image/jpeg", new byte[5 * 1024 * 1024 + 1]);
        var unsupported = new MockMultipartFile("file", "image.svg", "image/svg+xml", "<svg/>".getBytes());
        var spoofed = new MockMultipartFile("file", "image.jpg", "image/jpeg", "<html>invalid</html>".getBytes());
        assertThrows(RuntimeException.class, () -> uploads.uploadProfileImage(null, user.getEmail()));
        for (var file : new MockMultipartFile[]{empty, oversized, unsupported, spoofed}) {
            assertThrows(RuntimeException.class, () -> uploads.uploadProfileImage(file, user.getEmail()));
        }
        verify(users, never()).save(any());
    }
}
