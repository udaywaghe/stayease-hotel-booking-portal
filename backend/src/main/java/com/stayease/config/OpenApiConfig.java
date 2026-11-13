package com.stayease.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class OpenApiConfig {
    @Bean public OpenAPI stayEaseApi(){return new OpenAPI().info(new Info().title("StayEase Booking API").version("v1").description("Hotel discovery, availability, booking, cancellation, and personalized stay recommendations.").contact(new Contact().name("StayEase API")));}
}
