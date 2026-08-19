package org.example.ecommerceapplication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // Minimum 32 characters for HS256
    private static final String SECRET_KEY =
            "my-super-secret-jwt-key-12345678901234567890";

    // 1 hour
    private static final long JWT_EXPIRATION =
            1000 * 60 * 60;


    // =========================
    // Generate Token
    // =========================
    public String generateToken(UserDetails userDetails) {

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + JWT_EXPIRATION
                        )
                )
                .signWith(getSigningKey())
                .compact();
    }


    // =========================
    // Get Username
    // =========================
    public String extractUsername(String token) {

        return extractClaim(
                token,
                Claims::getSubject
        );
    }


    // =========================
    // Extract Claim
    // =========================
    public <T> T extractClaim(
            String token,
            Function<Claims, T> claimsResolver
    ) {

        Claims claims =
                extractAllClaims(token);

        return claimsResolver.apply(claims);
    }


    // =========================
    // Extract All Claims
    // =========================
    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    // =========================
    // Validate Token
    // =========================
    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        String username =
                extractUsername(token);

        return username.equals(
                userDetails.getUsername()
        ) && !isTokenExpired(token);
    }


    // =========================
    // Check Expiration
    // =========================
    private boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }


    // =========================
    // Get Expiration
    // =========================
    private Date extractExpiration(String token) {

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }


    // =========================
    // Signing Key
    // =========================
    private SecretKey getSigningKey() {

        byte[] keyBytes =
                SECRET_KEY.getBytes(
                        StandardCharsets.UTF_8
                );

        return Keys.hmacShaKeyFor(keyBytes);
    }
}