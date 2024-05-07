package io.vital.billspace.service.implementation;

import io.vital.billspace.model.Role;
import io.vital.billspace.repository.RoleRepository;
import io.vital.billspace.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository<Role> roleRepository;

    @Override
    public Role getRoleByUserId(Long id) {
        return roleRepository.getRoleByUserId(id);
    }
}
