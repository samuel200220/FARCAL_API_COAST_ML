package com.yaounde.farcal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${farcal.openapi.dev-url}")
    private String devUrl;

    @Value("${farcal.openapi.prod-url}")
    private String prodUrl;

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("URL de développement");

        Server prodServer = new Server();
        prodServer.setUrl(prodUrl);
        prodServer.setDescription("URL de production");

        Contact contact = new Contact();
        contact.setEmail("samuelftagat@gmail.com");
        contact.setName("Yaoundé Fare Calculator");
        contact.setUrl("https://farcal-api-coast-ml-spring.onrender.com");

        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Fare Calculator API")
                .version("1.0")
                .contact(contact)
                .description("API pour le calculateur de tarifs à Yaoundé")
                .termsOfService("https://farcal-api-coast-ml-spring.onrender.com/terms")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}