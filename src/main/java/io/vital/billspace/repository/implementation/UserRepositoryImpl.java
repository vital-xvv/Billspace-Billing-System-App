package io.vital.billspace.repository.implementation;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.enumeration.VerificationType;
import io.vital.billspace.exception.APIException;
import io.vital.billspace.form.UpdateUserForm;
import io.vital.billspace.model.Role;
import io.vital.billspace.model.User;
import io.vital.billspace.model.UserPrincipal;
import io.vital.billspace.repository.RoleRepository;
import io.vital.billspace.repository.UserRepository;
import io.vital.billspace.rowmapper.UserRowMapper;
import io.vital.billspace.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.dao.DataAccessException;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static io.vital.billspace.enumeration.RoleType.ROLE_USER;
import static io.vital.billspace.enumeration.VerificationType.ACCOUNT;
import static io.vital.billspace.enumeration.VerificationType.PASSWORD;
import static io.vital.billspace.query.ResetPasswordVerificationQuery.*;
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
            emailService.sendSimpleMailMessage(user.getFirstName(), user.getEmail(), verificationUrl);
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
        try {
            return jdbcTemplate.queryForObject(FIND_USER_BY_ID_QUERY, Map.of("id", id), new UserRowMapper());
        }catch (DataAccessException e){
            log.error(e.getMessage());
            throw new APIException("No user was found with id %d".formatted(id));
        }
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
    public User verifyAccountByKey(String key) {
        try{
            //Find a user with corresponding verification token
            User user = jdbcTemplate.queryForObject(GET_USER_WITH_TOKEN_ACCOUNT_VERIFICATION_QUERY,
                    Map.of("url", getVerificationUrl(key, ACCOUNT.getType())), new UserRowMapper());

            //Update a user with corresponding verification token set enabled = true
            jdbcTemplate.update(USER_VERIFICATION_ACCOUNT_ENABLED_QUERY, Map.of("userId", user.getId()));
            log.info("User verified with id: {}, token {}", user.getId(), key);

            //Delete verification token from AccountVerifications
            //TODO

            return user;
        // If any error occurs, throw exception with a proper message
        }catch(EmptyResultDataAccessException e) {
            throw new APIException("Verification Link is not valid");
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

    private SqlParameterSource getUserDetailsSQLParameterSource(UpdateUserForm user) {
        return new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("email", user.getEmail())
                .addValue("phone", user.getPhone())
                .addValue("address", user.getAddress())
                .addValue("title", user.getTitle())
                .addValue("bio", user.getBio());
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
            return new UserPrincipal(user, roleRepository.getRoleByUserId(user.getId()));
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
            throw new APIException("Invalid verification code. Please try again.");
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

    @Override
    public void resetPassword(String email) {
        if(getEmailCount(email) == 0)
            throw new APIException("There is no account for the email address.");
        try {
            String expirationDate = DateFormatUtils.format(DateUtils.addDays(new Date(), 1), DATE_FORMAT);
            User user = findByEmail(email);
            String verificationUrl = getVerificationUrl(UUID.randomUUID().toString(), PASSWORD.getType());
            jdbcTemplate.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, Map.of("user_id", user.getId()));
            jdbcTemplate.update(INSERT_PASSWORD_VERIFICATION_QUERY,
                    Map.of("id", user.getId(),
                            "url", verificationUrl,
                            "expirationDate", expirationDate));

            log.info("Verification URL: {}", verificationUrl);
            //send email with verification url to the user
            //TODO
        }catch (Exception ex) {
            log.error(ex.getMessage());
            throw new APIException("An error occurred. Please try again.");
        }
    }

    @Override
    public User verifyPasswordKey(String key) {
        if(isLinkExpired(key, PASSWORD)) throw new APIException("This link has expired. Please reset your password again.");
        try{
            String url = getVerificationUrl(key, PASSWORD.getType());
            User user = jdbcTemplate.queryForObject(SELECT_USER_BY_PASSWORD_URL_QUERY,
                    Map.of("url", url), new UserRowMapper());
            //jdbcTemplate.update(DELETE_PASSWORD_VERIFICATION_BY_USER_ID_QUERY, Map.of("user_id", user.getId()));
            return user;
        }catch(EmptyResultDataAccessException ex){
            log.error(ex.getMessage());
            throw new APIException("Invalid verification url. Please reset your password again.");
        }catch (Exception ex){
            log.error(ex.getMessage());
            throw new APIException("An error occurred. Please try again.");
        }
    }

    @Override
    public void renewPassword(String key, String password, String confirmPassword) {
        if(!password.equals(confirmPassword)) throw new APIException("Passwords don't match. Please try again.");
        String url = getVerificationUrl(key, PASSWORD.getType());
        try {
            jdbcTemplate.update(UPDATE_USER_PASSWORD_BY_VERIFICATION_URL_QUERY,
                    Map.of("url", url, "password", encoder.encode(password)));
            jdbcTemplate.update(DELETE_PASSWORD_VERIFICATION_BY_URL_QUERY, Map.of("url", url));
        }catch (Exception ex) {
            log.error(ex.getMessage());
            throw new APIException("An error occurred. Please try again.");
        }
    }

    @Override
    public User updateUserDetails(UpdateUserForm user) {
        try{
            jdbcTemplate.update( UPDATE_USER_DETAILS_QUERY,getUserDetailsSQLParameterSource(user));
            return get(user.getId());
        }catch (Exception e){
            log.error(e.getMessage());
            throw new APIException("Error occurred updating user. Please try again.");
        }
    }

    @Override
    public void updatePassword(Long userId, String currentPassword, String newPassword, String confirmPassword) {
        if(!newPassword.equals(confirmPassword)) throw new APIException("Passwords don't match. Please try again.");
        User user = get(userId);
        if(encoder.matches(currentPassword, user.getPassword())) {
            try {
                jdbcTemplate.update(UPDATE_USER_PASSWORD_BY_ID_QUERY,
                        Map.of("userId", user.getId(), "newPassword", encoder.encode(newPassword)));
            }catch (Exception e){
                throw new APIException("An error occurred updating password. Please try again.");
            }
        }else {
            throw new APIException("Incorrect current password. Please try again.");
        }
    }

    @Override
    public void updateRoleByUserId(Long userId, String roleName) {
        log.info("Updating role for User ID {}", userId);
        try{
            jdbcTemplate.update(UPDATE_USER_ROLE_BY_ROLENAME_USER_ID_QUERY, Map.of("userId", userId, "roleName", roleName));
        }catch (Exception e){
            log.error(e.getMessage());
            throw new APIException("Error occurred updating role for user. Please try again.");
        }
    }

    @Override
    public void updateUserAccountSettings(Long userId, Boolean enabled, Boolean nonLocked) {
        log.info("Updating user account settings with id: {}", userId);
        try{
            jdbcTemplate.update(UPDATE_USER_ACCOUNT_SETTINGS_QUERY, Map.of("userId", userId, "enabled", enabled, "nonLocked", nonLocked));
        }catch (DataAccessException e){
            log.error(e.getMessage());
            throw new APIException("User id %d does not exist".formatted(userId));
        }catch (Exception e){
            log.error(e.getMessage());
            throw new APIException("Error occurred updating user account settings. Please try again.");
        }
    }

    @Override
    public void toggleMfa(Long userId) {
        User user = get(userId);
        if(StringUtils.isBlank(user.getPhone())) {
            log.error("Can't switch to MFA 'cause phone number is absent.");
            throw new APIException("Can't switch to MFA 'cause phone number is absent.");
        }
        log.info("Changing MFA settings for user with id: {}", userId);
        try{
            jdbcTemplate.update(UPDATE_MFA_QUERY, Map.of("mfa", !user.isUsingMFA(), "userId", user.getId()));
        } catch (Exception e){
            log.error(e.getMessage());
            throw new APIException("Error occurred updating MFA settings. Please try again.");
        }
    }

    @Override
    public void updateAvatarImage(Long userId, MultipartFile image) {
        User user = get(userId);
        String imageUrl = getUserImageURL(user.getEmail());
        user.setImageURL(imageUrl);
        saveImage(user, image);
        jdbcTemplate.update(UPDATE_USER_IMAGE_QUERY, Map.of("imageUrl", imageUrl, "userId", userId));
    }

    private void saveImage(User user, MultipartFile image) {
        Path fileStorageLocation = Paths.get(System.getProperty("user.home") + "/Downloads/images/").toAbsolutePath().normalize();
        if(!Files.exists(fileStorageLocation)){
            try{
                Files.createDirectory(fileStorageLocation);
            }catch (IOException e){
                log.error(e.getMessage());
                throw new APIException("Unable to create directory to save image. Please try again.");
            }
        }
        log.info("Created directory {}", fileStorageLocation.toString());
        try{
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(user.getEmail() + ".png"), StandardCopyOption.REPLACE_EXISTING);
        }catch (IOException e){
            log.error(e.getMessage());
            throw new APIException("Error occurred saving an image. Please try again.");
        }
        log.info("Created file in folder {}", fileStorageLocation.toString());
    }

    private String getUserImageURL(String email) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/user/image/" + email + ".png")
                .toUriString();
    }

    private Boolean isLinkExpired(String key, VerificationType type) {
        try {
            Date date = jdbcTemplate.queryForObject(SELECT_EXPIRATION_DATE_BY_CODE_QUERY,
                    Map.of("url", getVerificationUrl(key, type.getType())), Date.class);
            return date.before(new Date());
        }catch (EmptyResultDataAccessException ex) {
            log.error(ex.getMessage());
            throw new APIException("Invalid verification url. Please reset your password again.");
        }catch (Exception e){
            throw new APIException("An error occurred. Please try again.");
        }
    }

    private String getVerificationUrl(String key, String type){
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/user/verify/" + type + "/" + key)
                .toUriString();
    }
}
