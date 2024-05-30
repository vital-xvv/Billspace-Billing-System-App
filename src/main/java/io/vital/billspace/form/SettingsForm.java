package io.vital.billspace.form;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingsForm {
    @NotNull(message = "Enabled can not be null.")
    private Boolean enabled;
    @NotNull(message = "NonLocked can not be null.")
    private Boolean nonLocked;
}
