package io.vital.billspace.repository.implementation;

import io.vital.billspace.exception.APIException;
import io.vital.billspace.model.Role;
import io.vital.billspace.model.User;
import io.vital.billspace.repository.RoleRepository;
import io.vital.billspace.repository.UserRepository;
import io.vital.billspace.rowmapper.RoleRowMapper;
import io.vital.billspace.service.EmailService;
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

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import io.vital.billspace.rowmapper.UserRowMapper;

import static io.vital.billspace.enumeration.RoleType.ROLE_USER;
import static io.vital.billspace.query.RoleQuery.INSERT_ROLE_TO_USER_QUERY;
import static io.vital.billspace.query.RoleQuery.SELECT_ROLE_BY_NAME_QUERY;
import static io.vital.billspace.query.UserQuery.*;
import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User> {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;

    @Override
    public User create(User user) {
        // Check email is Unique
        if(getEmailCount(user.getEmail().trim().toLowerCase()) > 0)
            throw new APIException("E-mail is already used. Please use different e-mail.");

        try{
            // Save new user
            KeyHolder keyHolder = new GeneratedKeyHolder();
            SqlParameterSource sqlParameterSource = getSQLParameterSource(user);
            jdbcTemplate.update(INSERT_USER_QUERY, sqlParameterSource, keyHolder);
            user.setId(requireNonNull(keyHolder.getKey()).longValue());


            // Add role to the user
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());

            // Send verification URL
            String token = UUID.randomUUID().toString();


            // Save URL in verification table
            jdbcTemplate.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), "token", token));


            // Send e-mail to the user with verification URL
            emailService.sendSimpleMailMessage(user.getFirstName(), user.getEmail(), token);

            // Return a newly created user
            return user;

        // If any error occurs, throw exception with a proper message
        }catch (Exception e) {
            log.error("Something went wrong.\n" + e.getMessage());
            throw new APIException("Error occurred. Please try again.");
        }
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

    @Override
    public Boolean verifyUserByToken(String token) {
        try{
            User user = jdbcTemplate.queryForObject(GET_USER_WITH_TOKEN_ACCOUNT_VERIFICATION_QUERY, Map.of("token", token), new UserRowMapper());
            jdbcTemplate.update(USER_VERIFICATION_ACCOUNT_ENABLED_QUERY, Map.of("userId", user.getId()));
            log.info("Verified User with id: {}, token {}", user.getId(), token);
            return Boolean.TRUE;
            // If any error occurs, throw exception with a proper message
        }catch(EmptyResultDataAccessException e) {
            throw new APIException("No user has been found by this token: " + ROLE_USER.name());
        }catch (Exception e) {
            throw new APIException("Error occurred. Please try again. Verifying User Account" + e.getMessage());
        }
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
