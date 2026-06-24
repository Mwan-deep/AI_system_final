package AI_Study_Hub.service;

import AI_Study_Hub.dto.request.AuthenticateRequest;
import AI_Study_Hub.dto.request.IntroSpecRequest;
import AI_Study_Hub.dto.request.LogoutRequest;
import AI_Study_Hub.dto.response.AuthenticateResponse;
import AI_Study_Hub.dto.response.IntroSpecResponse;
import AI_Study_Hub.exception.AppException;
import AI_Study_Hub.exception.ErrorCode;
import AI_Study_Hub.entity.Account;
import AI_Study_Hub.entity.InvalidatedtokenSession;
import AI_Study_Hub.repository.AccountRespository;
import AI_Study_Hub.repository.InvalidationRespository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticateService {
    @Value("${jwt.signerToken}")
    @NonFinal
    protected String SIGNER_TOKEN;
    AccountRespository accountRespository;
    InvalidationRespository invalidationRespository;


    public AuthenticateResponse authenticate (AuthenticateRequest request){
        var account = accountRespository.findAccountByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USERNAME_NOT_EXITED));


        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPasswordHash(), account.getPasswordHash());
        if(!authenticated){
            throw new AppException(ErrorCode.PASSWORD_INCORRECTLY);
        }
        passwordEncoder.encode(account.getPasswordHash());

        var token = generateToken(account);

        return AuthenticateResponse.builder()
                .token(token)
                .authenticated(authenticated)
                .build();
    }

    public String generateToken(Account account){
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getUserName())
                .issuer("devteria")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope" , buildScope(account))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_TOKEN.getBytes()));
            return jwsObject.serialize();
        }catch (JOSEException e){
            log.info("CAN NOT GET TOKEN");
            throw  new RuntimeException(e);
        }
    }

    public String buildScope(Account account){
        StringJoiner stringJoiner = new StringJoiner(" ");

        if(!CollectionUtils.isEmpty(account.getRoles())){
            account.getRoles().forEach(role -> stringJoiner.add("ROLE_"+role.getRoleId()));
        }
        return stringJoiner.toString();
    }

    public IntroSpecResponse introSpec (IntroSpecRequest request)
            throws JOSEException, ParseException{
        boolean isValid = true;

        try {
            verifyToken(request.getToken());
        }catch (AppException e) {
            isValid = false;
        }
        return IntroSpecResponse.builder()
                .authenticated(isValid)
                .build();
    }

    public SignedJWT verifyToken(String token)
            throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(SIGNER_TOKEN.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expireTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier);

        String jit = signedJWT.getJWTClaimsSet().getJWTID();

        boolean invalidatedToken = invalidationRespository.existsById(jit);

        if(!(verified && expireTime.after(new Date())))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if(invalidatedToken){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;

    }

    public void logout(LogoutRequest request)
            throws ParseException, JOSEException {
        try{
            var signedJWT = verifyToken(request.getToken());

            LocalDateTime expireTime =  signedJWT.getJWTClaimsSet()
                    .getExpirationTime()
                    .toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            String jit = signedJWT.getJWTClaimsSet().getJWTID();

            InvalidatedtokenSession invalidatedtokenSession = InvalidatedtokenSession.builder()
                    .invalidId(jit)
                    .expireTime(expireTime)
                    .build();

            invalidationRespository.save(invalidatedtokenSession);
        }catch (AppException e){
            log.info("Token already expired");
        }


    }
}
