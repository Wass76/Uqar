package com.Uqar.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info =@Info(
                contact = @Contact(
                        name = "Wassem Tenbakji" ,
                        email = "wasee.tenbakji@gmail.com"
                ),
                description = "open api documentation for Uqar pharmacy management system.",
                title = "Uqar pharmacy management system",
                version = "1.0",
                license = @License(
                        name = "All Copyrights reserved to Wassem Tenbakji",
                        url = "https://www.linkedin.com/in/wassem-tenbakji-a078a5206"
                ),
                termsOfService = "Term of my Service is 1  month after applying"
        ),
        servers =
                {
                        @Server(
                                description = "Prod ENV",
                                url = "https://api.uqarsoft.com"
                        ),
                        @Server(
                                description = "Local ENV",
                                url = "http://localhost:3000"
                        )
                },
        security = @SecurityRequirement(name = "BearerAuth")
)
@SecurityScheme(
        name = "BearerAuth",
        description = "JWT auth description",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
