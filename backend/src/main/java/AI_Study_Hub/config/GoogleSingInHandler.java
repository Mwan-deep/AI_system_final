package AI_Study_Hub.config;

import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.Role;
import AI_Study_Hub.exception.AppException;
import AI_Study_Hub.exception.ErrorCode;
import AI_Study_Hub.repository.AccountRespository;
import AI_Study_Hub.repository.RoleRespository;
import AI_Study_Hub.service.AuthenticateService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

@Configuration
@Component
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
public class GoogleSingInHandler implements AuthenticationSuccessHandler {
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    AccountRespository accountRespository;
    RoleRespository roleRespository;

    @Autowired
    AuthenticateService authenticateService;

    public GoogleSingInHandler(AccountRespository accountRespository, RoleRespository roleRespository, AuthenticateService authenticateService) {
        this.accountRespository = accountRespository;
        this.roleRespository = roleRespository;
        this.authenticateService = authenticateService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        if(oAuth2User == null) return;

        String email = oAuth2User.getAttribute("email").toString();
        if(email == null) return;
        String name = oAuth2User.getAttribute("name");

        HashSet<Role> roles = new HashSet<>();
        Role userRole = roleRespository.findById("USER").orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        roles.add(userRole);
        Account account = Account.builder()
                .userName(email)
                .fullName(name)
                .gender(null)
                .dob(null)
                .email(email)
                .avatarUrl(null)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .accountStatus("ACTIVE")
                .roles(roles)
                .bio(null)
                .build();

        accountRespository.save(account);
        String token = authenticateService.generateToken(account);

        //response.sendRedirect("https://localhost:5173//oauth2-success?token=" + token);
        response.setContentType("application/json");
        response.getWriter().write("""
                                   {
                                    "authenticated": true,
                                    "token": "%s"
                                   }
                                   """.formatted(token));

    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        AuthenticationSuccessHandler.super.onAuthenticationSuccess(request, response, chain, authentication);
    }
}
