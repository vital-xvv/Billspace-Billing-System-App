package io.vital.billspace.repository;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.form.UpdateUserForm;
import io.vital.billspace.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

public interface UserRepository<T extends User> {
    /*Basic CRUD Operations*/
    T create(T entity);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T entity);
    Boolean delete(T entity);

    /*Complex SQL queries*/
    T verifyAccountByKey(String token);
    T findByEmail(String email);

    void sendVerificationCode(UserDto user);

    T verifyCode(String email, String code);

    void resetPassword(String email);

    T verifyPasswordKey(String key);

    void renewPassword(String key, String password, String confirmPassword);

    T updateUserDetails(UpdateUserForm user);

    void updatePassword(Long userId, String currentPassword, String newPassword, String confirmPassword);

    void updateRoleByUserId(Long userId, String roleName);

    void updateUserAccountSettings(Long userId, Boolean enabled, Boolean nonLocked);

    void toggleMfa(Long userId);

    void updateAvatarImage(Long userId, MultipartFile image);
}
