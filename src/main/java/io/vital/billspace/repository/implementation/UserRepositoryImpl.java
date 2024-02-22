package io.vital.billspace.repository.implementation;

import io.vital.billspace.model.Role;
import io.vital.billspace.model.User;
import io.vital.billspace.repository.RoleRepository;
import io.vital.billspace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import io.vital.billspace.exception.APIException;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static io.vital.billspace.query.UserQuery.*;
import static io.vital.billspace.enumeration.RoleType.*;

import static java.util.Objects.requireNonNull;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User> {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    public User create(User user) {
        // Check email is Unique
        if(getEmailCount(user.getEmail().trim().toLowerCase()) > 0)
            throw new APIException("E-mail is already used. Please use different e-mail.");
        // Save new user
        try{
            KeyHolder keyHolder = new GeneratedKeyHolder();
            SqlParameterSource parameterSource = getSQLParameterSource(user);
            jdbcTemplate.update(INSERT_USER_QUERY, parameterSource, keyHolder);
            user.setId(requireNonNull(keyHolder.getKey()).longValue());
            // Add role to the user
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());
            // Send verification URL
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());
            // Save URL in verification table
            // Send e-mail to the user with verification URL
            // Return a newly created user
            // If any error occurs, throw exception with a proper message
        }catch(EmptyResultDataAccessException e) {

        }catch (Exception e) {

        }


        return null;
    }

    @Override
    public Collection<User> list(int page, int pageSize) {
        return null;
    }

    @Override
    public User get(Long id) {
        return null;
    }

    @Override
    public User update(User entity) {
        return null;
    }

    @Override
    public Boolean delete(User entity) {
        return null;
    }

    private Integer getEmailCount(String email) {
        return jdbcTemplate.queryForObject(COUNT_USER_EMAIL_QUERY, Map.of("email", email), Integer.class);
    }

    private SqlParameterSource getSQLParameterSource(User user) {
        return new MapSqlParameterSource()
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("password", encoder.encode(user.getPassword()));
    }
}
