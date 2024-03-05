package io.vital.billspace.repository.implementation;

import io.vital.billspace.exception.APIException;
import io.vital.billspace.model.Role;
import io.vital.billspace.model.User;
import io.vital.billspace.repository.RoleRepository;
import io.vital.billspace.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import io.vital.billspace.rowmapper.RoleRowMapper;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.vital.billspace.enumeration.RoleType.ROLE_USER;
import static io.vital.billspace.query.RoleQuery.*;

@Repository
@AllArgsConstructor
@Slf4j
public class RoleRepositoryImpl implements RoleRepository<Role> {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Role create(Role entity) {
        return null;
    }

    @Override
    public Collection<Role> list(int page, int pageSize) {
        return null;
    }

    @Override
    public Role get(Long id) {
        return null;
    }

    @Override
    public Role update(Role entity) {
        return null;
    }

    @Override
    public Boolean delete(Role entity) {
        return null;
    }

    @Override
    public void addRoleToUser(Long userId, String roleName) {
        try{
            Role role = jdbcTemplate.queryForObject(SELECT_ROLE_BY_NAME_QUERY, Map.of("roleName", roleName), new RoleRowMapper());
            jdbcTemplate.update(INSERT_ROLE_TO_USER_QUERY, Map.of("userId", userId, "roleId", Objects.requireNonNull(role.getId())));
            log.info("Adding Role {} to the User with id: {}", roleName, userId);

            // If any error occurs, throw exception with a proper message
        }catch(EmptyResultDataAccessException e) {
            throw new APIException("No role found by name: " + ROLE_USER.name());
        }catch (Exception e) {
            throw new APIException("Error occurred. Please try again. Adding Role" + e.getMessage());
        }
    }

    @Override
    public Role getRoleByUserId(Long userId) {
        return null;
    }

    @Override
    public Role getRoleByUserEmail(String email) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {

    }
}
