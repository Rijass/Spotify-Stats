package com.spotifywrapped.spotify_wrapped_clone.service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Spotify Wrapped Clone API",
                version = "v1",
                description = "OpenAPI documentation for the Spotify Wrapped Clone backend"
        )
)
public class OpenApiConfig {
}
