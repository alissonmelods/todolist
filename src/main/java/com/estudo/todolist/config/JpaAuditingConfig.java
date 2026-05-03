package com.estudo.todolist.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita o JPA Auditing em uma classe de configuração separada para que
 * o slice {@code @WebMvcTest} não tente inicializar o contexto JPA
 * (evitando o erro "Cannot resolve reference to bean 'jpaMappingContext'").
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
