package com.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS configuration for local development.
 * Allows all origins, methods, and headers.
 */
@Configuration
@Profile("local")
public class CorsConfig {

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();
    
    // Allow all origins
    config.addAllowedOriginPattern("*");
    
    // Allow all HTTP methods
    config.addAllowedMethod("*");
    
    // Allow all headers
    config.addAllowedHeader("*");
    
    // Allow credentials (cookies, authorization headers)
    config.setAllowCredentials(true);
    
    // How long the preflight response can be cached
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    
    return new CorsFilter(source);
  }
}
