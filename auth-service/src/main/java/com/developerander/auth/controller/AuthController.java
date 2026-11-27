package com.developerander.auth.controller;

import com.developerander.auth.dto.LoginRequest;
import com.developerander.auth.dto.LoginResponse;
import com.developerander.auth.entity.UsuarioEntity;
import com.developerander.auth.repository.UsuarioRepository;
import com.developerander.auth.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Emisión de tokens JWT con roles institucionales")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Operation(summary = "Iniciar sesión", description = "Valida credenciales contra colegio_auth_db y devuelve token JWT, rol y nombre.")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        UsuarioEntity usuario = usuarioRepository.findByUsername(request.getUsername()).orElse(null);

        if (usuario == null || !usuario.isActivo() || !passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            log.warn("🚫 Login fallido para usuario: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "status", 401,
                            "error", "Unauthorized",
                            "mensaje", "Usuario o contraseña incorrectos"
                    ));
        }

        String token = jwtService.generarToken(usuario);
        log.info("✅ Login exitoso | Usuario: {} | Rol: {}", usuario.getUsername(), usuario.getRol());

        return ResponseEntity.ok(new LoginResponse(
                token,
                usuario.getRol().name(),
                usuario.getNombreCompleto()
        ));
    }
}
