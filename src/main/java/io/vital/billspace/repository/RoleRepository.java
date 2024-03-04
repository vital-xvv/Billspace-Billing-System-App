package io.vital.billspace.repository;

import io.vital.billspace.model.Role;

import java.util.Collection;

public interface RoleRepository<T extends Role> {
    /*Basic CRUD Operations*/

    T create(T entity);
    Collection<T> list(int page, int pageSize);
    T get(Long id);
    T update(T entity);
    Boolean delete(T entity);

    /*Complex SQL queries*/

    void addRoleToUser(Long userId, String roleName);
    Role getRoleByUserId(Long userId);
    Role getRoleByUserEmail(String email);
    void updateUserRole(Long userId, String roleName);
}
