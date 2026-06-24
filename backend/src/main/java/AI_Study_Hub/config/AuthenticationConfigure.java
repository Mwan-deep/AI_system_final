package AI_Study_Hub.config;

import AI_Study_Hub.exception.AppException;
import AI_Study_Hub.exception.ErrorCode;
import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.Role;
import AI_Study_Hub.repository.AccountRespository;
import AI_Study_Hub.repository.RoleRespository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationConfigure {
    RoleRespository roleRespository;
    AccountRespository accountRespository;


    @Bean
    ApplicationRunner applicationRunner (){
        return args -> {

            if(accountRespository.findAccountByUserName("Admin").isEmpty()){

                HashSet<Role> roles = new HashSet<>();
                Role adminRole = roleRespository.findRoleByRoleId("ADMIN");
                if(adminRole == null){
                    throw new AppException(ErrorCode.NOT_FOUND);
                }
                roles.add(adminRole);

                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);


                Account account = Account.builder()
                        .userName("Admin")
                        .passwordHash(passwordEncoder.encode("12345"))
                        .fullName("Tran Ngoc Duc")
                        .dob(LocalDate.of(2004, 12, 10))
                        .gender("MALE")
                        .email("tnducc7ntmkhai@gmail.com")
                        .avatarUrl(null)
                        .bio("AI STUDY HUB DEVELOPERS")
                        .createdAt(LocalDateTime.now())
                        .accountStatus("ACTIVE")
                        .roles(roles)
                        .build();

                accountRespository.save(account);
                log.warn("admin human has been created with default password: admin,please change it ");

            }
        };
    }

}
