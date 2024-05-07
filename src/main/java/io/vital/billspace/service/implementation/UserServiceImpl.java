package io.vital.billspace.service.implementation;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.dto.dtomapper.UserDtoMapper;
import io.vital.billspace.model.Role;
import io.vital.billspace.model.User;
import io.vital.billspace.repository.RoleRepository;
import io.vital.billspace.repository.UserRepository;
import io.vital.billspace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository<User> userRepository;
    private final RoleRepository<Role> roleRepository;

    @Override
    public UserDto createUser(User user) {
        return mapToUserDto(userRepository.create(user));
    }

    @Override
    public UserDto getUserByEmail(String email) {
        return mapToUserDto(userRepository.findByEmail(email));
    }

    @Override
    public Boolean verifyUser(String token) {
        return userRepository.verifyUserByToken(token);
    }

    @Override
    public void sendVerificationCode(UserDto user) {
        userRepository.sendVerificationCode(user);
    }

    @Override
    public UserDto verifyCode(String email, String code) {
        return mapToUserDto(userRepository.verifyCode(email, code));
    }

    private UserDto mapToUserDto(User user){
        return UserDtoMapper.of(user, roleRepository.getRoleByUserId(user.getId()));
    }
}
