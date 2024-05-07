package io.vital.billspace.repository.implementation;

import io.vital.billspace.exception.APIException;
import io.vital.billspace.model.Role;
import io.vital.billspace.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
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
    public Role getRoleByUserId(Long userId) {
        try{
            log.info("Finding role of the user with id: {}", userId);

            return jdbcTemplate.queryForObject(SELECT_ROLE_BY_USER_ID_QUERY,
                    Map.of("user_id", userId), new RoleRowMapper());

            // If any error occurs, throw exception with a proper message
        }catch (Exception e) {
            throw new APIException("Error occurred. Please try again. Finding role" + e.getMessage());
        }
    }

    @Override
    public Role getRoleByUserEmail(String email) {
        return null;
    }

    @Override
    public void updateUserRole(Long userId, String roleName) {}

    @Override
    public void addRoleToUser(Long userId, String roleName) {
        try{
            Role role = jdbcTemplate.queryForObject(SELECT_ROLE_BY_NAME_QUERY,
                    Map.of("roleName", roleName), new RoleRowMapper());

            assert role != null;

            jdbcTemplate.update(INSERT_ROLE_TO_USER_QUERY,
                    Map.of("userId", userId, "roleId", Objects.requireNonNull(role.getId())));

            log.info("Adding role {} to the user with id: {}", roleName, userId);

        // If any error occurs, throw exception with a proper message
        }catch (Exception e) {
            throw new APIException("Error occurred. Please try again. Adding role" + e.getMessage());
        }
    }
}
