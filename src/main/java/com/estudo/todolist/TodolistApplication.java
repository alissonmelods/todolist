package com.estudo.todolist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Ponto de entrada da aplicação TodoList.
 *
 * <p>{@code @EnableJpaAuditing} ativa o preenchimento automático dos campos
 * {@code @CreatedDate} e {@code @LastModifiedDate} nas entidades JPA,
 * dispensando atribuição manual de timestamps.</p>
 */
@SpringBootApplication
@EnableJpaAuditing
public class TodolistApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodolistApplication.class, args);
    }
}
