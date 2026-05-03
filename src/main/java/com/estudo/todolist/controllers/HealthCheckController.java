package com.estudo.todolist.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller responsável pelo health check da aplicação.
 *
 * <p>Expõe o endpoint {@code GET /health} para verificar se a aplicação está
 * operacional e qual versão está em execução. Útil para monitoramento,
 * orquestradores de contêiner (ex: Kubernetes liveness probe) e pipelines de CI/CD.</p>
 */
@RestController
@RequestMapping("/health")
public class HealthCheckController {

    /** Versão da aplicação injetada a partir do {@code pom.xml} via Maven resource filtering. */
    @Value("${spring.application.version}")
    private String version;

    /**
     * Retorna o status operacional e a versão da aplicação.
     *
     * @return {@code 200 OK} com {@code status: "UP"} e a versão atual do artefato Maven.
     */
    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "version", version
        ));
    }
}
