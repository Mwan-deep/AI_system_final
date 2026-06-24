package AI_Study_Hub.dto.request;
import AI_Study_Hub.Validation.DobConstraints;
import AI_Study_Hub.Validation.EmailContraints;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountCreateRequest {
    @Size(min = 5 , message = "PASSWORD_INVALIDATION")
    String passwordHash;
    @Size(min = 2 , message = "Name must be at leats 2 characterist")
    String fullName;
    @EmailContraints(message = "EMAIL_INVALID")
    String email;
    @DobConstraints(min = 7, message = "INVALID_DOB")
    LocalDate dob;
    String gender;
    String avatarUrl;
    String bio;
}
