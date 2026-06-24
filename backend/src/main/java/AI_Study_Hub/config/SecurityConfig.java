package AI_Study_Hub.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer; // <--- IMPORT MỚI
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {
    static String[] PUBLIC_ENDPOINT = {
            "/api/account",
            "/api/authen",
            "/api/authen/introspec",
            "/api/auth/**",
            "/api/v1/share/download/**",
            "/api/v1/master-data/**",
            "/api/v1/rankings/**"
    };
    static String[] MUST_BE_AUTHENTICATE = {"/api/account/change-password", "/api/authen/logout"};

    // ĐÃ XÓA BIẾN: private HttpSecurity http;

    @Autowired
    CustomJwtDecoder customJwtDecoder;

    @Autowired
    CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // ĐÃ SỬA DÒNG NÀY: Dùng mặc định để Spring tự động nối với file CorsConfig.java
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requestMatcherRegistry ->
                        requestMatcherRegistry
                                .requestMatchers(PUBLIC_ENDPOINT).permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/account/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/account/**").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/account/createAccountByAdmin").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, MUST_BE_AUTHENTICATE).authenticated()
                                .anyRequest()
                                .authenticated())
                .exceptionHandling(exception ->
                        exception.accessDeniedHandler(customAccessDeniedHandler))
        ;

        httpSecurity.oauth2ResourceServer(oAuth2 ->
                oAuth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(customJwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())));

        return httpSecurity.build();
    }

    public JwtAuthenticationConverter jwtAuthenticationConverter(){
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return authenticationConverter;
    }
}