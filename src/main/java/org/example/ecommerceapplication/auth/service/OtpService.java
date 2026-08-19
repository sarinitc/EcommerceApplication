package org.example.ecommerceapplication.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    private final SecureRandom secureRandom =
            new SecureRandom();


    // =========================
    // SEND OTP
    // =========================
    public void sendOtp(String email) {

        // 1. Generate 6-digit OTP
        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        // 2. Redis key
        String key = "otp:" + email;

        // 3. Store OTP in Redis for 5 minutes
        redisTemplate.opsForValue().set(
                key,
                otp,
                Duration.ofMinutes(5)
        );

        // 4. Send OTP to real email
        emailService.sendOtp(
                email,
                otp
        );
    }


    // =========================
    // VERIFY OTP
    // =========================
    public boolean verifyOtp(
            String email,
            String otp
    ) {

        String key = "otp:" + email;

        // 1. Get OTP from Redis
        String storedOtp =
                redisTemplate
                        .opsForValue()
                        .get(key);

        // 2. OTP expired or doesn't exist
        if (storedOtp == null) {
            return false;
        }

        // 3. Wrong OTP
        if (!storedOtp.equals(otp)) {
            return false;
        }

        // 4. Correct OTP
        // Delete so it can't be used again
        redisTemplate.delete(key);

        return true;
    }
}