package AI_Study_Hub.controller;

import AI_Study_Hub.dto.response.ApiResponse;
import AI_Study_Hub.dto.request.AuthenticateRequest;
import AI_Study_Hub.dto.request.IntroSpecRequest;
import AI_Study_Hub.dto.request.LogoutRequest;
import AI_Study_Hub.dto.response.AuthenticateResponse;
import AI_Study_Hub.dto.response.IntroSpecResponse;
import AI_Study_Hub.service.AuthenticateService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api/authen")
public class AuthenticateController {

    @Autowired
    AuthenticateService authenticateService;

    @PostMapping
    ApiResponse<AuthenticateResponse> authenticate (@RequestBody AuthenticateRequest request){
        var result = authenticateService.authenticate(request);
        return ApiResponse.<AuthenticateResponse>builder()
                .result(result)
                .build();
    }
    @PostMapping("/introspec")
    ApiResponse<IntroSpecResponse> introSpec (@RequestBody IntroSpecRequest request) throws ParseException, JOSEException {
        var result = authenticateService.introSpec(request);
        return ApiResponse.<IntroSpecResponse>builder()
                .result(result)
                .build();
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticateService.logout(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Logout Successfully!!!")
                .result(null)
                .build();
    }
}
