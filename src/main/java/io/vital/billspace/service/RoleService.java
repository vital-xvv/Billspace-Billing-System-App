package io.vital.billspace.service;

import io.vital.billspace.model.Role;

public interface RoleService {
    Role getRoleByUserId(Long id);
}
