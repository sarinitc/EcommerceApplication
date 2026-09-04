package org.example.ecommerceapplication.auth.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.ecommerceapplication.auth.exception.OtpException;
import org.springframework.http.HttpStatus;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetOtpService {

    private final StringRedisTemplate redisTemplate;
    private final Clock clock;

    private static final String PREFIX =
            "forgot-password:";

    private static final Duration OTP_EXPIRATION =
            Duration.ofMinutes(5);
    private static final Duration EXPIRY_METADATA_RETENTION =
            Duration.ofMinutes(10);

    private final SecureRandom secureRandom =
            new SecureRandom();

    // =====================================================
    // GENERATE + STORE OTP
    // =====================================================

    public String generateOtp(String email) {

        // Generate number between 100000 and 999999
        int number =
                100000 + secureRandom.nextInt(900000);

        String otp =
                String.valueOf(number);


        // Example Redis key:
        // forgot-password:cheavsarin0@gmail.com
        String key = otpKey(email);
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(5, ChronoUnit.MINUTES);

        // A resend replaces both the pending OTP and any prior verification.
        clearOtp(email);


        // Store OTP in Redis for 5 minutes
        redisTemplate
                .opsForValue()
                .set(
                        key,
                        otp,
                        OTP_EXPIRATION
                );
        redisTemplate.opsForValue().set(
                expiryKey(email),
                expiresAt.toString(),
                EXPIRY_METADATA_RETENTION
        );

        log.debug("Password-reset OTP createdAt={}, expiresAt={}", now, expiresAt);


        return otp;
    }


    // =====================================================
    // VERIFY OTP
    // =====================================================

    public void verifyOtp(
            String email,
            String otp
    ) {
        String key = otpKey(email);
        Instant now = Instant.now(clock);
        Instant expiresAt = getExpiresAt(email);

        log.debug("Password-reset OTP verify currentTime={}", now);

        if (isVerified(email)) {
            throw new OtpException("OTP has already been used.", HttpStatus.BAD_REQUEST);
        }

        if (expiresAt == null) {
            throw new OtpException("OTP not found.", HttpStatus.NOT_FOUND);
        }

        if (now.isAfter(expiresAt)) {
            redisTemplate.delete(key);
            throw new OtpException("OTP has expired.", HttpStatus.GONE);
        }


        // Read OTP from Redis
        String storedOtp =
                redisTemplate
                        .opsForValue()
                        .get(key);


        if (storedOtp == null) {
            throw new OtpException("OTP not found.", HttpStatus.NOT_FOUND);
        }


        if (!storedOtp.equals(otp)) {
            throw new OtpException("Invalid OTP.", HttpStatus.BAD_REQUEST);
        }

        Duration remaining = Duration.between(now, expiresAt);
        if (remaining.isZero() || remaining.isNegative()) {
            redisTemplate.delete(key);
            throw new OtpException("OTP has expired.", HttpStatus.GONE);
        }

        redisTemplate.delete(key);
        redisTemplate.opsForValue().set(verifiedKey(email), "verified", remaining);
        log.debug("Password-reset OTP verified; verification expiresAt={}", expiresAt);
    }


    // =====================================================
    // DELETE OTP
    // =====================================================

    public void deleteOtp(String email) {
        clearOtp(email);
    }

    public boolean hasPasswordResetRequest(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(otpKey(email)))
                || Boolean.TRUE.equals(redisTemplate.hasKey(verifiedKey(email)))
                || Boolean.TRUE.equals(redisTemplate.hasKey(expiryKey(email)));
    }

    public void requireVerifiedOtp(String email) {
        if (!isVerified(email)) {
            throw new OtpException(
                    "OTP verification required before resetting password.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }
    private Instant getExpiresAt(String email) {
        String rawExpiry = redisTemplate.opsForValue().get(expiryKey(email));
        return rawExpiry == null ? null : Instant.parse(rawExpiry);
    }

    private boolean isVerified(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(verifiedKey(email)));
    }

    private void clearOtp(String email) {
        redisTemplate.delete(otpKey(email));
        redisTemplate.delete(verifiedKey(email));
        redisTemplate.delete(expiryKey(email));
    }

    private String otpKey(String email) {
        return PREFIX + email.trim().toLowerCase();
    }

    private String verifiedKey(String email) {
        return otpKey(email) + ":verified";
    }

    private String expiryKey(String email) {
        return otpKey(email) + ":expires-at";
    }
}
