package AI_Study_Hub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
       CorsConfiguration corsConfiguration = new CorsConfiguration();
       corsConfiguration.addAllowedOrigin("http://localhost:5173");
       corsConfiguration.addAllowedHeader("*");
       corsConfiguration.addAllowedMethod("*");

       UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
       urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

       return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}