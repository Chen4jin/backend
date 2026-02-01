package com.api.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS configuration for all environments.
 * In production: allows specific origins (chenjq.com)
 * In local: allows all origins
 */
@Configuration
public class CorsConfig {

  @Value("${spring.profiles.active:}")
  private String activeProfile;

  @Bean
  public CorsFilter corsFilter() {
    CorsConfiguration config = new CorsConfiguration();

    if ("local".equals(activeProfile)) {
      // Local development - allow all origins
      config.addAllowedOriginPattern("*");
    } else {
      // Production - allow specific origins
      List<String> allowedOrigins = Arrays.asList(
          "https://chenjq.com",
          "https://www.chenjq.com",
          "https://api.chenjq.com"
      );
      config.setAllowedOrigins(allowedOrigins);
    }

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
