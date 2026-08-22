package com.alzain.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {

    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklistToken(String token, long expirationMs) {
        if (token == null || token.trim().isEmpty()) return;
        String hash = hashToken(token.trim());
        blacklistedTokens.put(hash, expirationMs);
        log.info("Token successfully blacklisted/revoked until {}", expirationMs);
    }

    public boolean isBlacklisted(String token) {
        if (token == null || token.trim().isEmpty()) return false;
        String hash = hashToken(token.trim());
        Long exp = blacklistedTokens.get(hash);
        if (exp == null) {
            return false;
        }
        if (System.currentTimeMillis() > exp) {
            blacklistedTokens.remove(hash);
            return false;
        }
        return true;
    }

    @Scheduled(fixedRate = 3600000) // Purge expired entries hourly
    public void purgeExpiredTokens() {
        long now = System.currentTimeMillis();
        int initialSize = blacklistedTokens.size();
        blacklistedTokens.entrySet().removeIf(entry -> now > entry.getValue());
        int purged = initialSize - blacklistedTokens.size();
        if (purged > 0) {
            log.info("Purged {} expired tokens from revocation denylist.", purged);
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(token.hashCode());
        }
    }
}
