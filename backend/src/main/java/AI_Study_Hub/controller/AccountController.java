package AI_Study_Hub.controller;

import AI_Study_Hub.dto.request.AccountCreateRequest;
import AI_Study_Hub.dto.request.AccountUpdateRequest;
import AI_Study_Hub.dto.response.ApiResponse;
import AI_Study_Hub.dto.request.ChangePasswordRequest;
import AI_Study_Hub.dto.response.AccountResponse;
import AI_Study_Hub.dto.response.ChangePasswordResponse;
import AI_Study_Hub.entity.Account;
import AI_Study_Hub.service.AccountService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class AccountController {
    @Autowired
    AccountService accountService;

    @PostMapping
    ApiResponse<AccountResponse> createAccount(@RequestBody @Valid AccountCreateRequest request){
        var result = accountService.createAccount(request);
        return ApiResponse.<AccountResponse>builder()
                .message("Create Successfully!!!")
                .result(result)
                .build();
    }
    @PutMapping("/{id}")
    ApiResponse<AccountResponse> updateProfile(@RequestBody AccountUpdateRequest request, @PathVariable Long id){
        var result = accountService.updateAccount(request, id);
        return ApiResponse.<AccountResponse>builder()
                .message("Update Successfully!!!")
                .result(result)
                .build();
    }
    @DeleteMapping("/{id}")
    ApiResponse<Void>  deleteAccount(@PathVariable Long id){
        accountService.deleteAccount(id);
        return ApiResponse.<Void>builder()
                .message("Delete Successfully!!!")
                .result(null)
                .build();
    }
    @GetMapping
    ApiResponse<List<Account>> ManageUser(){
        var result = accountService.getAllAccount();
        return ApiResponse.<List<Account>>builder()
                .result(result)
                .build();
    }
    @PostMapping("/change_password")
    ApiResponse<ChangePasswordResponse> changePassword(@RequestBody  ChangePasswordRequest request){
        var result = accountService.changePassword(request);
        return ApiResponse.<ChangePasswordResponse>builder()
                .message("Change Password Successfully!!!")
                .result(result)
                .build();
    }
    @PostMapping("/createAccountByAdmin")
    ApiResponse<AccountResponse> createAccountByAdmin(@RequestBody @Valid AccountCreateRequest request){
        var result = accountService.createAccountByAdmin(request);
        return ApiResponse.<AccountResponse>builder()
                .message("Create Successfully!!!")
                .result(result)
                .build();
    }
}
