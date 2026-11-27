package com.developerander.auth.security;

import com.developerander.auth.entity.UsuarioEntity;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.security.jwt.secret:colegio-secret-key-apf3-2026-muy-larga-256bits}")
    private String jwtSecret;

    @Value("${app.security.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    public String generarToken(UsuarioEntity usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(usuario.getUsername())
                .claims(Map.of(
                        "rol", usuario.getRol().name(),
                        "nombre", usuario.getNombreCompleto()
                ))
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
