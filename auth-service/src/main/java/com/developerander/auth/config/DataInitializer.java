package com.developerander.auth.config;

import com.developerander.auth.entity.RolUsuario;
import com.developerander.auth.entity.UsuarioEntity;
import com.developerander.auth.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initUsers(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.count() == 0) {
                String passHash = encoder.encode("123456");
                repo.save(crear("a0000000-0000-0000-0000-000000000001", "secretaria", passHash, "María Elena Gómez (Secretaría)", RolUsuario.SECRETARIA));
                repo.save(crear("a0000000-0000-0000-0000-000000000002", "tesoreria",  passHash, "Carlos Alberto Ruiz (Tesorería)", RolUsuario.TESORERIA));
                repo.save(crear("a0000000-0000-0000-0000-000000000003", "docente",    passHash, "Lucía Fernanda Torres (Docente)", RolUsuario.DOCENTE));
                repo.save(crear("a0000000-0000-0000-0000-000000000004", "direccion",  passHash, "Mg. Roberto Chávez (Dirección)", RolUsuario.DIRECCION));
                repo.save(crear("a0000000-0000-0000-0000-000000000005", "apoderado",  passHash, "Carlos García (Apoderado)", RolUsuario.APODERADO));
                log.info("✅ Usuarios semilla creados en colegio_auth_db (password: 123456)");
            }
        };
    }

    private UsuarioEntity crear(String id, String username, String passHash, String nombre, RolUsuario rol) {
        UsuarioEntity u = new UsuarioEntity();
        u.setIdUsuario(id);
        u.setUsername(username);
        u.setPassword(passHash);
        u.setNombreCompleto(nombre);
        u.setRol(rol);
        u.setActivo(true);
        return u;
    }
}
