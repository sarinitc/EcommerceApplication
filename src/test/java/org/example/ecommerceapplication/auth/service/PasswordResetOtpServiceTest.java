package org.example.ecommerceapplication.auth.service;

import org.example.ecommerceapplication.auth.exception.OtpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasswordResetOtpServiceTest {

    private static final String EMAIL = "user@gmail.com";
    private static final Instant CREATED_AT = Instant.parse("2026-08-31T05:48:00Z");

    private final Map<String, String> redis = new HashMap<>();
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenAnswer(invocation -> redis.get(invocation.getArgument(0)));
        when(redisTemplate.hasKey(anyString())).thenAnswer(invocation -> redis.containsKey(invocation.getArgument(0)));
        doAnswer(invocation -> {
            redis.remove(invocation.getArgument(0));
            return true;
        }).when(redisTemplate).delete(anyString());
        doAnswer(invocation -> {
            redis.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(valueOperations).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void correctOtpIsAcceptedImmediatelyAndBeforeFiveMinutes() {
        putOtp("123456", CREATED_AT);

        assertDoesNotThrow(() -> serviceAt(CREATED_AT).verifyOtp(EMAIL, "123456"));
        assertFalse(redis.containsKey(otpKey()));

        putOtp("654321", CREATED_AT);
        assertDoesNotThrow(() -> serviceAt(CREATED_AT.plus(Duration.ofMinutes(4))).verifyOtp(EMAIL, "654321"));
    }

    @Test
    void correctOtpIsAcceptedAfterOneMinute() {
        putOtp("123456", CREATED_AT);

        assertDoesNotThrow(() -> serviceAt(CREATED_AT.plus(Duration.ofMinutes(1))).verifyOtp(EMAIL, "123456"));
    }

    @Test
    void correctOtpAfterFiveMinutesIsExpired() {
        putOtp("123456", CREATED_AT);

        OtpException exception = assertThrows(
                OtpException.class,
                () -> serviceAt(CREATED_AT.plus(Duration.ofMinutes(5)).plusSeconds(1)).verifyOtp(EMAIL, "123456")
        );

        assertEquals("OTP has expired.", exception.getMessage());
    }

    @Test
    void wrongOtpIsRejected() {
        putOtp("123456", CREATED_AT);

        OtpException exception = assertThrows(
                OtpException.class,
                () -> serviceAt(CREATED_AT.plusSeconds(30)).verifyOtp(EMAIL, "000000")
        );

        assertEquals("Invalid OTP.", exception.getMessage());
    }

    @Test
    void resendInvalidatesOldOtpAndNewOtpCanBeVerified() {
        putOtp("111111", CREATED_AT);
        PasswordResetOtpService service = serviceAt(CREATED_AT.plus(Duration.ofMinutes(2)));
        String replacementOtp = service.generateOtp(EMAIL);

        OtpException exception = assertThrows(
                OtpException.class,
                () -> service.verifyOtp(EMAIL, "111111")
        );
        assertEquals("Invalid OTP.", exception.getMessage());
        assertDoesNotThrow(() -> service.verifyOtp(EMAIL, replacementOtp));
    }

    @Test
    void resetAuthorizationCannotBeReusedAfterItIsCleared() {
        putOtp("123456", CREATED_AT);
        PasswordResetOtpService service = serviceAt(CREATED_AT.plusSeconds(30));
        service.verifyOtp(EMAIL, "123456");
        service.requireVerifiedOtp(EMAIL);

        service.deleteOtp(EMAIL);

        assertThrows(OtpException.class, () -> service.requireVerifiedOtp(EMAIL));
    }

    private PasswordResetOtpService serviceAt(Instant instant) {
        return new PasswordResetOtpService(redisTemplate, Clock.fixed(instant, ZoneOffset.UTC));
    }

    private void putOtp(String otp, Instant createdAt) {
        redis.remove(otpKey() + ":verified");
        redis.put(otpKey(), otp);
        redis.put(expiryKey(), createdAt.plus(Duration.ofMinutes(5)).toString());
    }

    private String otpKey() {
        return "forgot-password:" + EMAIL;
    }

    private String expiryKey() {
        return otpKey() + ":expires-at";
    }
}
