package io.vital.billspace.service;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.form.UpdateUserForm;
import io.vital.billspace.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserDto createUser(User user);
    UserDto getUserByEmail(String email);
    UserDto verifyAccountByKey(String token);

    void sendVerificationCode(UserDto user);
    UserDto verifyCode(String email, String code);

    void resetPassword(String email);

    UserDto verifyPasswordKey(String key);

    void renewPassword(String key, String password, String confirmPassword);

    UserDto updateUserDetails(UpdateUserForm user);

    UserDto getUserById(Long userId);

    void updatePassword(Long userId, String currentPassword, String newPassword, String confirmPassword);

    void updateRole(Long userId, String roleName);

    void updateAccountSettings(Long userId, Boolean enabled, Boolean nonLocked);

    void toggleMfa(Long userId);

    void updateAvatarImage(Long userId, MultipartFile image);
}
