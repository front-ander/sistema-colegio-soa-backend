package com.developerander.microservice.colegio.client;

import com.developerander.microservice.colegio.dto.VacanteResponse;
import com.developerander.microservice.colegio.entity.Aula;
import com.developerander.microservice.colegio.repository.AulaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class AulaClient {

    private static final Logger log = LoggerFactory.getLogger(AulaClient.class);

    @Autowired
    private AulaRepository aulaRepository;

    public Optional<VacanteResponse> consultarVacantes(UUID idAula) {
        return aulaRepository.findById(idAula).map(aula -> {
            int matriculados = Math.max(0, aula.getCapacidadMaxima() - aula.getVacantes());
            log.info("🏫 [BD-AULAS] Aula {} ({} {}): {} vacantes",
                    aula.getIdAula(), aula.getGrado(), aula.getSeccion(), aula.getVacantes());
            return new VacanteResponse(
                    aula.getIdAula().toString(),
                    aula.getGrado() + " - " + aula.getSeccion(),
                    aula.getCapacidadMaxima(),
                    matriculados,
                    aula.getVacantes()
            );
        });
    }
}
