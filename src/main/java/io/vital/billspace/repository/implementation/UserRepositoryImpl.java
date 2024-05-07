package io.vital.billspace.repository.implementation;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.dto.dtomapper.UserDtoMapper;
import io.vital.billspace.exception.APIException;
import io.vital.billspace.model.Role;
import io.vital.billspace.model.User;
import io.vital.billspace.model.UserPrincipal;
import io.vital.billspace.repository.RoleRepository;
import io.vital.billspace.repository.UserRepository;
import io.vital.billspace.rowmapper.UserRowMapper;
import io.vital.billspace.service.EmailService;
import io.vital.billspace.utils.SmsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static io.vital.billspace.enumeration.RoleType.ROLE_USER;
import static io.vital.billspace.enumeration.VerificationType.*;
import static io.vital.billspace.query.TwoFactorVerificationQuery.*;
import static io.vital.billspace.query.UserQuery.*;
import static java.util.Objects.requireNonNull;


@Repository
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository<User>, UserDetailsService {
    public static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final RoleRepository<Role> roleRepository;
    private final BCryptPasswordEncoder encoder;
    private final EmailService emailService;

    @Override
    public User create(User user) {
        // Check email is Unique
        if(getEmailCount(user.getEmail().trim().toLowerCase()) > 0)
            throw new APIException("E-mail is already used. Please use a different e-mail.");
        // Save new user
        try{
            KeyHolder keyHolder = new GeneratedKeyHolder();
            SqlParameterSource sqlParameterSource = getSQLParameterSource(user);
            jdbcTemplate.update(INSERT_USER_QUERY, sqlParameterSource, keyHolder);
            user.setId(requireNonNull(keyHolder.getKey()).longValue());

            // Add role to the user
            roleRepository.addRoleToUser(user.getId(), ROLE_USER.name());

            // Send verification URL
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), ACCOUNT.getType());

            // Save URL in verification table
            jdbcTemplate.update(INSERT_ACCOUNT_VERIFICATION_URL_QUERY, Map.of("userId", user.getId(), "url", verificationUrl));

            // Send e-mail to the user with verification URL
            //emailService.sendVerificationUrl(user.getFirstName(), user.getEmail(), verificationUrl, ACCOUNT);
            user.setEnabled(false);
            user.setNotLocked(true);

            // Return a newly created user
            return user;

        // If any error occurs, throw exception with a proper message
        }
        catch (EmptyResultDataAccessException ex){
            throw new APIException("No role found by name: " + ROLE_USER.name());
        }
        catch (Exception e) {
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
            //Find a user with corresponding verification token
            User user = jdbcTemplate.queryForObject(GET_USER_WITH_TOKEN_ACCOUNT_VERIFICATION_QUERY, Map.of("token", token), new UserRowMapper());

            //Update a user with corresponding verification token set enabled = true
            jdbcTemplate.update(USER_VERIFICATION_ACCOUNT_ENABLED_QUERY, Map.of("userId", user.getId()));
            log.info("Verified User with id: {}, token {}", user.getId(), token);

            //Delete verification token from AccountVerifications
            //TODO

            return Boolean.TRUE;

        // If any error occurs, throw exception with a proper message
        }catch(EmptyResultDataAccessException e) {
            throw new APIException("No user has been found by this token: " + ROLE_USER.name());
        }catch (Exception e) {
            throw new APIException("Error occurred. Please try again. Verifying User Account" + e.getMessage());
        }
    }

    @Override
    public User findByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject(GET_USER_BY_EMAIL_QUERY, Map.of("email", email), new UserRowMapper());
        }catch (EmptyResultDataAccessException e){
            throw new APIException("No user found by email: " + email);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new APIException("An error occurred. Please try again.");
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

    private String getVerificationUrl(String key, String type){
        return ServletUriComponentsBuilder.fromCurrentContextPath().path("/user/verify/" + type + "/" + key).toUriString();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //Find user by email
        User user = findByEmail(email);
        if(user == null) {
            log.error("User not found in the database: {}.", email);
            throw new UsernameNotFoundException("User not found in the database.");
        } else {
            log.info("User found in database: {}.", email);
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()).getPermission());
        }
    }

    @Override
    public void sendVerificationCode(UserDto user) {
        String expirationDate = DateFormatUtils.format(DateUtils.addDays(new Date(), 1), DATE_FORMAT);
        String verificationCode = RandomStringUtils.randomAlphabetic(8).toUpperCase();

        try {
            jdbcTemplate.update(DELETE_VERIFICATION_CODE_BY_USER_ID_QUERY, Map.of("id", user.getId()));
            jdbcTemplate.update(INSERT_VERIFICATION_CODE_QUERY, Map.of("user_id", user.getId(), "code",
                    verificationCode, "expiration_date", expirationDate));

            //SmsUtils.sendSMS(user.getPhone(), "Billspace\nVerification code\n" + verificationCode);
        }catch (Exception e){
            log.error(e.getMessage());
            throw new APIException("An error occurred. Please try again.");
        }
    }

    @Override
    public User verifyCode(String email, String code) {
        if(isVerificationCodeExpired(email, code))
            throw new APIException("Verification code %s has expired. Please log in again.".formatted(code));
        try {
            User user =  jdbcTemplate.queryForObject(SELECT_USER_BY_USER_CODE_QUERY,
                    Map.of("code", code, "email", email),
                    new UserRowMapper());
            jdbcTemplate.update(DELETE_CODE, Map.of("code", code));
            return user;
        }catch (EmptyResultDataAccessException ex) {
            log.error("Invalid verification code for User with email: {}", email);
            throw new APIException(ex.getMessage());
        }
    }

    private Boolean isVerificationCodeExpired(String email, String code) {
        try {
            Date date = jdbcTemplate.queryForObject(SELECT_EXPIRATION_DATE_BY_USER_CODE_QUERY,
                    Map.of("code", code, "email", email), Date.class);
            return date.before(new Date());
        }catch (EmptyResultDataAccessException ex) {
            log.error("Invalid verification code for User with email: {}", email);
            throw new APIException(ex.getMessage());
        }
    }
}
