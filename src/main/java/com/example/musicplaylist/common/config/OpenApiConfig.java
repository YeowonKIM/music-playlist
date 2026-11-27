package com.example.musicplaylist.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI musicPlaylistOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Music Playlist Management API")
                        .description("RESTful API for playlist management, shuffling, recommendations, export, and playback control.")
                        .version("1.0.0"));
    }
}