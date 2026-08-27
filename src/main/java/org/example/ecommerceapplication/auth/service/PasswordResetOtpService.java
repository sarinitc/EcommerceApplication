package org.example.ecommerceapplication.auth.service;



import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PasswordResetOtpService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX =
            "forgot-password:";

    private static final Duration OTP_EXPIRATION =
            Duration.ofMinutes(5);

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
        String key =
                PREFIX + email.toLowerCase();


        // Store OTP in Redis for 5 minutes
        redisTemplate
                .opsForValue()
                .set(
                        key,
                        otp,
                        OTP_EXPIRATION
                );


        return otp;
    }


    // =====================================================
    // VERIFY OTP
    // =====================================================

    public boolean verifyOtp(
            String email,
            String otp
    ) {

        String key =
                PREFIX + email.toLowerCase();


        // Read OTP from Redis
        String storedOtp =
                redisTemplate
                        .opsForValue()
                        .get(key);


        // OTP expired or does not exist
        if (storedOtp == null) {
            return false;
        }


        // Compare client OTP with stored OTP
        return storedOtp.equals(otp);
    }


    // =====================================================
    // DELETE OTP
    // =====================================================

    public void deleteOtp(String email) {

        String key =
                PREFIX + email.toLowerCase();

        redisTemplate.delete(key);
    }
}