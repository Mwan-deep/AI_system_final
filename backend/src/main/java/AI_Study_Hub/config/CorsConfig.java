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
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 1. Cho phép gửi Cookie và Token xác thực
        config.setAllowCredentials(true);

        // 2. CHỈ ĐỊNH RÕ RÀNG PORT CỦA FRONTEND (Chìa khóa giải quyết lỗi của bạn)
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));

        // 3. Cho phép tất cả các Header (Authorization, Content-Type...)
        config.setAllowedHeaders(Arrays.asList("*"));

        // 4. Cho phép tất cả các phương thức gọi API (GET, POST, PUT, PATCH, DELETE)
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 5. Áp dụng cấu hình này cho mọi API (/**)
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}