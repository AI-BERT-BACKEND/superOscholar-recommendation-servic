package com.aibert.dosw.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI openApi() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Recommendation Service API — AIBERT")
                                                .description("""
                                                                AI-powered recommendation engine that generates personalised study recommendations, daily plans, and weekly reorganisation suggestions for AIBERT ECI Planner students.

                                                                **Key responsibilities:**
                                                                - Generate 1-5 personalised AI recommendations (Groq/Mistral) based on student workload and productivity patterns (R18)
                                                                - Build daily study plans with up to 5 prioritised tasks and reorganisation suggestions (R19, AIB-29)
                                                                - Produce weekly reorganisation proposals identifying overloaded days (>80 % capacity) and proposing up to 5 concrete task moves (R20, AIB-30)

                                                                Authentication: all endpoints are currently public (no Bearer JWT required in this environment). A SecurityScheme is declared for future-proofing; use the Authorize button once auth is enforced.

                                                                Local testing: see `swagger-tests/swagger-tests-guide.md` in the repository for ready-to-paste curl commands and a JWT token generation guide.

                                                                Contact AI-BERT Backend Team
                                                                """)
                                                .version("v1.0")
                                                .contact(new Contact()
                                                                .name("AI-BERT Backend Team")
                                                                .url("https://github.com/AI-BERT-BACKEND")))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("Bearer JWT token issued by the auth service")));
        }
}
