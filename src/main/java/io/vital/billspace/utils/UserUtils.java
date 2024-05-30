package io.vital.billspace.utils;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.model.UserPrincipal;
import org.springframework.security.core.Authentication;

public class UserUtils {
    public static UserDto getAuthenticatedUser(Authentication authentication){
        return ((UserDto) authentication.getPrincipal());
    }

    public static UserDto getLoggedInUser(Authentication authentication){
        return ((UserPrincipal)authentication.getPrincipal()).getUser();
    }
}
