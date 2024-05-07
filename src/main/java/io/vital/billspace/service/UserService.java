package io.vital.billspace.service;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.model.User;

public interface UserService {
    UserDto createUser(User user);
    UserDto getUserByEmail(String email);
    Boolean verifyUser(String token);

    void sendVerificationCode(UserDto user);
    UserDto verifyCode(String email, String code);
}
