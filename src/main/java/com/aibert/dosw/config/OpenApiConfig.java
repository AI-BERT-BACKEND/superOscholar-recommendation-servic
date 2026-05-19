package com.aibert.dosw.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Value("${server.port:8086}")
        private int serverPort;

        @Bean
        public OpenAPI openApi() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Recommendation Service API")
                                                .description("""
                                                                Motor de Recomendaciones Inteligente — A.IBERT ECI Planner.

                                                                **Funcionalidades:**
                                                                - **R18** `POST /api/v1/recommendations` — Genera 1-5 recomendaciones personalizadas con IA (Groq/Mistral).
                                                                - **R19** `GET /api/v1/recommendations/daily/{studentId}` — Plan diario con tareas priorizadas y sugerencias de reorganización.
                                                                - **R20** `GET /api/v1/recommendations/weekly/{studentId}` — Reorganización semanal: días sobrecargados y propuestas de movimiento.

                                                                **Nota:** Todos los endpoints son públicos (no requieren token en este entorno).
                                                                """)
                                                .version("v1.0")
                                                .contact(new Contact()
                                                                .name("AI-BERT Backend Team")
                                                                .url("https://github.com/AI-BERT-BACKEND")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:" + serverPort)
                                                                .description("Local Development")));
        }
}
