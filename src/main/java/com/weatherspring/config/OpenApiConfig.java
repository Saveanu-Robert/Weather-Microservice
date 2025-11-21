package com.weatherspring.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration for OpenAPI/Swagger documentation.
 *
 * <p>Provides comprehensive API documentation accessible at /swagger-ui.html</p>
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    /**
     * Configures OpenAPI documentation.
     *
     * @return OpenAPI configuration
     */
    @Bean
    public OpenAPI weatherServiceOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:" + serverPort);
        localServer.setDescription("Local development server");

        Contact contact = new Contact();
        contact.setName("Weather Service Team");
        contact.setEmail("support@weatherservice.com");

        License license = new License();
        license.setName("MIT License");
        license.setUrl("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("Weather Microservice API")
                .version("1.0.0")
                .description("Production-ready weather microservice providing current weather data, " +
                           "forecasts, and historical weather records. " +
                           "Integrates with WeatherAPI.com for real-time data.")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
