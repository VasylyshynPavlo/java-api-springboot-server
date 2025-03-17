package org.example.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.example.entities.UserEntity;
import org.example.repository.IUserRoleRepository;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

import static java.lang.String.format;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final IUserRoleRepository userRoleRepository;
    private final String SECRET_KEY = "de0fa6915d2a8ccfd5c6904e34d84398199cb0ffe18539bbb6b8760ad80b99dc9e846770b32da18abf931de7a89f31bc812392e709b64f813cc40029c344ec2fb88302726d6ea2b4091cd8d84458ca22e948a5adb4a706910472b8aa1662190bca1209ecd737ea25d10de779cbf79577902e6ad30a0d67d03372db4b97b54405f75fb3572a9b05acedf9b7d616b20bdbafc3ff137b1777b5ab74ba67ce6cc4c77a83d78cb6dc0c5f6e6c08c0464a8c6c9794c16a52e369edfbf3937cd26bebd677da23e30e96d2af9444829ca35a6e6292a7fb6db6ad89afe301ca8104dbad8f3ea11e903d4ee510cf3e7e09a2bf6cd3d811540b253c0f943e54b8c0b932f3a4";

    public String generateAccessToken(UserEntity user) {
        var roles = userRoleRepository.findByUser(user);
        Date currentDate = new Date();
        Date expireDate = new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);

        SecretKey key = getSecretKey();

        return Jwts.builder()
                .subject(format("%s,%s", user.getId(), user.getUsername()))
                .claim("email", user.getUsername())
                .claim("roles", roles.stream()
                        .map((role) -> role.getRole().getName()).toArray(String []:: new))
                .issuedAt(new Date())
                .expiration(expireDate)
                .signWith(key)
                .compact();
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject().split(",")[0];
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject().split(",")[1];
    }

    public Date getExpirationDate(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }

    public boolean validate(String token) {
        try {
            SecretKey key = getSecretKey();
            Jwts.parser().verifyWith(key).build().parse(token);
            return true;
        } catch (SignatureException ex) {
            System.out.println("Invalid JWT signature - "+ ex.getMessage());
        } catch (MalformedJwtException ex) {
            System.out.println("Invalid JWT token - " + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            System.out.println("Expired JWT token - " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            System.out.println("Unsupported JWT token - " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.out.println("JWT claims string is empty - " + ex.getMessage());
        }
        return false;
    }
}