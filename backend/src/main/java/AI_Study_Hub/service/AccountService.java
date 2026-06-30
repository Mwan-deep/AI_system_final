package AI_Study_Hub.service;

import AI_Study_Hub.dto.request.AccountCreateRequest;
import AI_Study_Hub.dto.request.AccountUpdateRequest;
import AI_Study_Hub.dto.request.ChangePasswordRequest;
import AI_Study_Hub.dto.response.AccountResponse;
import AI_Study_Hub.dto.response.ChangePasswordResponse;
import AI_Study_Hub.entity.Device;
import AI_Study_Hub.exception.AppException;
import AI_Study_Hub.exception.ErrorCode;
import AI_Study_Hub.Mapper.AccountMapper;
import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.Role;
import AI_Study_Hub.repository.AccountRespository;
import AI_Study_Hub.repository.DeviceRepository;
import AI_Study_Hub.repository.RoleRespository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AccountService {
    AccountRespository accountRespository;
    RoleRespository roleRespository;
    DeviceRepository deviceRepository;

    AccountMapper accountMapper;

    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request){
        try{
            if(accountRespository.existsAccountsByEmail(request.getEmail()))
                throw  new AppException((ErrorCode.EMAIL_EXITED));

            Account account = accountMapper.toAccount(request);

            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
            account.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));

            HashSet<Role> roles = new HashSet<>();
            Role userRole = roleRespository.findById("USER")
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

            roles.add(userRole);
            account.setRoles(roles);

            account.setCreatedAt(LocalDateTime.now());

            account.setAccountStatus("ACTIVE");

            account.setUserName("user_" + UUID.randomUUID().toString());

            account = accountRespository.save(account);

            if(request.getDeviceId() != null && !request.getDeviceId().isEmpty()){
                Device device = new Device();
                device.setId(UUID.randomUUID().toString());
                device.setDeviceId(request.getDeviceId());
                device.setTrusted(true);
                device.setLastLogin(LocalDateTime.now().toString());

                device.setAccountId(account.getAccountId());

                deviceRepository.save(device);
            }
            return accountMapper.toAccountResponse(account);

        }catch (DataIntegrityViolationException exception){
            log.error("Database integrity violation during account creation", exception);
            throw new AppException(ErrorCode.EMAIL_EXITED);
        }
    }
    public AccountResponse updateAccount(AccountUpdateRequest request , Long id){
        Account account = accountRespository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

        accountMapper.toUpdateAccount(request, account);

        account.setUpdatedAt(LocalDateTime.now());

        accountRespository.save(account);

        return accountMapper.toAccountResponse(account);
    }
    public void deleteAccount(Long id){
        Account account = accountRespository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));

        accountRespository.delete(account);
    }
    public List<Account> getAllAccount(){
        return accountRespository.findAll();
    }

    public ChangePasswordResponse changePassword(ChangePasswordRequest request){

        //kiem tra co dung nguoi dung ko do ben SecurityContextHollder giu
        var userName = SecurityContextHolder.getContext().getAuthentication().getName();

        Account account = accountRespository.findAccountByUserName(userName)
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_EXITED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        //Kiem tra mk cu co dung voi mk cu trong he thong hay ko
        boolean isOldPasswordCorrect = passwordEncoder
                .matches(request.getOldPassword(), account.getPasswordHash());

        if(!isOldPasswordCorrect){
            throw new AppException(ErrorCode.PASSWORD_INCORRECTLY);
        }

        //kiem tra 1 lan nua la confirm lai mk moi
        if(!request.getNewPassword().equals(request.getConfirmNewPassword())){
            throw new AppException(ErrorCode.NEW_PASSWORD_INCORRECTLY);
        }

        //Ma hoa mk moi
        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());

        account.setPasswordHash(newPasswordHash);

        accountRespository.save(account);

        return ChangePasswordResponse.builder()
                .changed(true)
                .build();
    }

    public AccountResponse createAccountByAdmin(AccountCreateRequest request){
        var account = accountMapper.toAccount(request);

        if(accountRespository.existsAccountsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.EMAIL_NOT_EXITS);
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        account.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));

        HashSet<Role> roles = new HashSet<>();
        Role managerRole = roleRespository.findById("MANAGER")
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        roles.add(managerRole);

        account.setUserName("manager");

        account.setRoles(roles);

        return accountMapper.toAccountResponse(accountRespository.save(account));

    }
    public AccountResponse GetAccountById(Long id){
        var account = accountRespository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_EXITS));
        return accountMapper.toAccountResponse(account);
    }
}
