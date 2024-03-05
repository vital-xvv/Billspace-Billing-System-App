package io.vital.billspace.service;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.model.HttpResponse;
import io.vital.billspace.model.User;

public interface UserService {
    UserDto createUser(User user);

    Boolean verifyUser(String token);
}
