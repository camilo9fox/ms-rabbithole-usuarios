package com.rabbithole.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@ComponentScan({"com.rabbithole.usuarios"})
@EntityScan({"com.rabbithole.usuarios.model"})
@EnableJpaRepositories({"com.rabbithole.usuarios.repository"})
public class UsuariosApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsuariosApplication.class, args);
	}
	
	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

}
