package AI_Study_Hub.entity;

import AI_Study_Hub.entity.Role;
import AI_Study_Hub.Validation.DobConstraints;
import AI_Study_Hub.Validation.EmailContraints;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "password_hash", nullable = false)
    @Size(min = 5 , message = "PASSWORD_INVALIDATION")
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false)
    @EmailContraints(message = "EMAIL_INVALID")
    private String email;

    @Column(name = "dob" , nullable = false)
    @DobConstraints(min = 7, message = "INVALID_DOB")
    private LocalDate dob;

    @Column(name = "gender", length = 10, nullable = false)
    private String gender;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Lob
    @Column(name = "bio")
    private String bio;

    @ColumnDefault("'ACTIVE'")
    @Column(name = "account_status", length = 20)
    private String accountStatus;

    @ColumnDefault("getdate()")
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ColumnDefault("getdate()")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "account_role",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )

    private Set<Role> roles = new HashSet<>();

    @ManyToOne
    private Set<Device> devices = new HashSet<>();

}