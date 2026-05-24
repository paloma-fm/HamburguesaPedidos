package com.proyectoduoc.hamburgueseria_pedidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class HamburgueseriaPedidosApplication {

	public static void main(String[] args) {
		SpringApplication.run(HamburgueseriaPedidosApplication.class, args);
	}

	// Este bloque hace que el RestTemplate funcione en el Service
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}