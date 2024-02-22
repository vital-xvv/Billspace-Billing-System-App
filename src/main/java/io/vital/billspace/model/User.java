package io.vital.billspace.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_DEFAULT)
public class User {

    private Long id;
    @NotEmpty(message = "First Name cannot be empty.")
    private String firstName;
    @NotEmpty(message = "Last Name cannot be empty.")
    private String lastName;
    @NotEmpty(message = "Password cannot be empty.")
    private String password;
    @NotEmpty(message = "Email cannot be empty.")
    @Email(message = "Invalid format for E-mail")
    private String email;
    private String address;
    private String phone;
    private String title;
    private String bio;
    private String imageURL;
    private boolean enabled;
    private boolean isNotLocked;
    private boolean isUsingMFA;
    private LocalDateTime createdAt;

}
