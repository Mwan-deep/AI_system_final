package AI_Study_Hub.service;

import AI_Study_Hub.dto.request.ForgetPasswordRequest;
import AI_Study_Hub.dto.request.ResetPasswordRequest;
import AI_Study_Hub.exception.AppException;
import AI_Study_Hub.exception.ErrorCode;
import AI_Study_Hub.entity.OtpVerification;
import AI_Study_Hub.repository.AccountRespository;
import AI_Study_Hub.repository.OtpVerificationRespository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpService {
    OtpVerificationRespository otpVerificationRespository;
    AccountRespository accountRespository;
    EmailService emailService;

    //Chong spam
    Map<String, LocalDateTime> cooldown = new HashMap<>();

    //Kiem tra so lan user nhap sai
    Map<String, Integer> attempts = new HashMap<>();

    public String generarteOtp(){
        Random random = new Random();
        int otp = 100000 + (random.nextInt(900000));
        return String.valueOf(otp);
    }

    //Forget Password

    public void forgetPassword(ForgetPasswordRequest request){
        var account = accountRespository.findAccountByEmail(request.getGmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_EXITS));

        if(cooldown.containsKey(request.getGmail())){
            if(cooldown.get(request.getGmail()).isAfter(LocalDateTime.now())){
                throw new AppException(ErrorCode.TOO_MANY_REQUEST);
            }
        }

        cooldown.put(request.getGmail(), LocalDateTime.now().plusMinutes(1));

        String otp = generarteOtp();

        otpVerificationRespository.save(OtpVerification.builder()
                .gmail(request.getGmail())
                .otp(otp)
                .expireTime(LocalDateTime.now().plus(1, ChronoUnit.MINUTES))
                .build());

        String emailContent = "Xin chào,\n\n"
                + "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản tại AI_Study_Hub.\n"
                + "Mã OTP xác thực của bạn là: " + otp + "\n\n"
                + "Lưu ý: Mã OTP này chỉ có hiệu lực trong vòng 1 phút. "
                + "Vui lòng tuyệt đối không chia sẻ mã này cho bất kỳ ai.\n\n"
                + "Trân trọng,\n"
                + "Đội ngũ phát triển AI_Study_Hub";

        emailService.sendGmail(
                request.getGmail(), // Nhớ check lại là request.getEmail() hay getGmail() nhé
                "[AI_Study_Hub] Mã OTP Đặt Lại Mật Khẩu",
                emailContent
        );
    }

    //Reset Password
    public void resetPassword(ResetPasswordRequest request){
        var otpData = otpVerificationRespository.findById(request.getGmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP));

        // 1. check expire
        if(otpData.getExpireTime().isBefore(LocalDateTime.now())){
            otpVerificationRespository.delete(otpData);
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        // 2. init attempts
        int count = attempts.getOrDefault(request.getGmail(), 0);
        if(count >= 5){
            throw new AppException(ErrorCode.TOO_MANY_REQUEST);
        }

        // 3. check OTP
        if(!otpData.getOtp().equals(request.getOtp())){
            attempts.put(request.getGmail(), count + 1);
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        // 4. check password confirm
        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new AppException(ErrorCode.NEW_PASSWORD_INCORRECTLY);
        }

        // 5. update password
        var account = accountRespository.findAccountByEmail(request.getGmail())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        account.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        accountRespository.save(account);

        otpVerificationRespository.delete(otpData);
        attempts.remove(request.getGmail());

    }
}
