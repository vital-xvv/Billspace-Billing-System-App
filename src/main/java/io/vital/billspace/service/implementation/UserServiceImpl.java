package io.vital.billspace.service.implementation;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.dto.dtomapper.UserDtoMapper;
import io.vital.billspace.model.User;
import io.vital.billspace.repository.UserRepository;
import io.vital.billspace.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository<User> userUserRepository;

    @Override
    public UserDto createUser(User user) {
        return UserDtoMapper.of(userUserRepository.create(user));
    }
}
