package com.altamira.hotel;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "Hotel Altamira", version = "1.0",
        description = "API de recepción, hospedajes y facturación"))
@SpringBootApplication
public class HotelAltamira {
    public static void main(String[] args) {
        SpringApplication.run(HotelAltamira.class, args);
    }
}
