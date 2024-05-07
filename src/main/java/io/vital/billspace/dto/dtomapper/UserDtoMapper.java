package io.vital.billspace.dto.dtomapper;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.model.Role;
import io.vital.billspace.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

public class UserDtoMapper {
    public static UserDto of(User user) {
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        return userDto;
    }

    public static UserDto of(User user, Role role){
        UserDto userDto = new UserDto();
        BeanUtils.copyProperties(user, userDto);
        userDto.setPermissions(role.getPermission());
        userDto.setRoleName(role.getName());
        return userDto;
    }

    public static User toUser(UserDto userDto) {
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        return user;
    }
}
