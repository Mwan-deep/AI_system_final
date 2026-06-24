package AI_Study_Hub.controller;

import AI_Study_Hub.dto.response.ApiResponse;
import AI_Study_Hub.dto.request.ForgetPasswordRequest;
import AI_Study_Hub.dto.request.ResetPasswordRequest;
import AI_Study_Hub.service.OtpService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthControlller {
    OtpService otpService;

    @PostMapping("/forgot-password")
    ApiResponse<Void> forgetPassword(@RequestBody ForgetPasswordRequest request){
        otpService.forgetPassword(request);
        return ApiResponse.<Void>builder()
                .message("OTP sent")
                .build();
    }

    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request){
        otpService.resetPassword(request);
        return ApiResponse.<Void>builder()
                .message("Password reset successful!!!!")
                .build();
    }
}
