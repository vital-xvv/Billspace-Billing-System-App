package io.vital.billspace.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserForm {
    @NotNull(message = "ID cannot be null or empty.")
    private Long id;
    @NotEmpty(message = "First Name cannot be empty.")
    private String firstName;
    @NotEmpty(message = "Last Name cannot be empty.")
    private String lastName;
    @NotEmpty(message = "Email cannot be empty.")
    @Email(message = "Invalid email format.")
    private String email;
    private String address;
    @Pattern(regexp = "^\\d{12}$", message = "Invalid phone number")
    private String phone;
    private String title;
    private String bio;
}
