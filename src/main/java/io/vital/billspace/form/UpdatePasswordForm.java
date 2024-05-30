package io.vital.billspace.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordForm {
    @NotBlank(message = "Current password can not be empty or null")
    private String currentPassword;
    @NotBlank(message = "New password can not be empty or null")
    private String newPassword;
    @NotBlank(message = "Confirmation password can not be empty or null")
    private String confirmPassword;
}
