package io.vital.billspace.service.implementation;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.dto.dtomapper.UserDtoMapper;
import io.vital.billspace.form.UpdateUserForm;
import io.vital.billspace.model.Role;
import io.vital.billspace.model.User;
import io.vital.billspace.repository.RoleRepository;
import io.vital.billspace.repository.UserRepository;
import io.vital.billspace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    public UserDto verifyAccountByKey(String key) {
        return mapToUserDto(userRepository.verifyAccountByKey(key));
    }

    @Override
    public void sendVerificationCode(UserDto user) {
        userRepository.sendVerificationCode(user);
    }

    @Override
    public UserDto verifyCode(String email, String code) {
        return mapToUserDto(userRepository.verifyCode(email, code));
    }

    @Override
    public void resetPassword(String email) {
        userRepository.resetPassword(email);
    }

    @Override
    public UserDto verifyPasswordKey(String key) {
        return mapToUserDto(userRepository.verifyPasswordKey(key));
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        userRepository.renewPassword(key, password, confirmPassword);
    }

    @Override
    public UserDto updateUserDetails(UpdateUserForm user) {
        return mapToUserDto(userRepository.updateUserDetails(user));
    }

    @Override
    public UserDto getUserById(Long userId) {
        return mapToUserDto(userRepository.get(userId));
    }

    private UserDto mapToUserDto(User user){
        return UserDtoMapper.of(user, roleRepository.getRoleByUserId(user.getId()));
    }

    @Override
    public void updatePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        userRepository.updatePassword(userId, currentPassword, newPassword, confirmPassword);
    }

    @Override
    public void updateRole(Long userId, String roleName) {
        userRepository.updateRoleByUserId(userId, roleName);
    }

    @Override
    public void updateAccountSettings(Long userid, Boolean enabled, Boolean nonLocked) {
        userRepository.updateUserAccountSettings(userid, enabled, nonLocked);
    }

    @Override
    public void toggleMfa(Long userId) {
        userRepository.toggleMfa(userId);
    }

    @Override
    public void updateAvatarImage(Long userId, MultipartFile image) {
        userRepository.updateAvatarImage(userId, image);
    }
}
