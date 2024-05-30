package io.vital.billspace.controller;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.dto.dtomapper.UserDtoMapper;
import io.vital.billspace.exception.APIException;
import io.vital.billspace.form.LoginForm;
import io.vital.billspace.form.SettingsForm;
import io.vital.billspace.form.UpdatePasswordForm;
import io.vital.billspace.form.UpdateUserForm;
import io.vital.billspace.model.HttpResponse;
import io.vital.billspace.model.User;
import io.vital.billspace.model.UserPrincipal;
import io.vital.billspace.provider.TokenProvider;
import io.vital.billspace.service.RoleService;
import io.vital.billspace.service.UserService;
import io.vital.billspace.utils.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;

import static io.vital.billspace.filter.CustomAuthorizationFilter.TOKEN_PREFIX;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authManager;
    private final TokenProvider tokenProvider;
    private final RoleService roleService;
    private final HttpServletRequest httpRequest;
    private final HttpServletResponse httpResponse;

    // START - For login

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm){
        Authentication auth = authenticate(loginForm.getEmail(), loginForm.getPassword());
        UserDto userDto = UserUtils.getLoggedInUser(auth);
        return userDto.isUsingMFA() ? sendVerificationCode(userDto) : sendResponse(userDto);
    }

    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> saveUser(@PathVariable("email") String email,
                                                 @PathVariable("code") String code){
        UserDto user = userService.verifyCode(email, code);
        return sendResponse(user);
    }

    // END - For login

    // START - Registration

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user){
        UserDto userDto = userService.createUser(user);
        return ResponseEntity.created(getUri()).body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userDto))
                        .httpStatus(HttpStatus.CREATED)
                        .message("User has been created.")
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
    }


    @GetMapping("/verify/account/{key}")
    public ResponseEntity<HttpResponse> verifyAccount(@PathVariable String key){
        UserDto userDto = userService.verifyAccountByKey(key);
        return ResponseEntity.ok()
                .body(HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .httpStatus(HttpStatus.OK)
                        .message(userDto.isEnabled() ? "User account is already verified" : "User account has been verified.")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    // END - Registration

    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication){
        UserDto user = userService.getUserByEmail(UserUtils.getAuthenticatedUser(authentication).getEmail());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", user, "roles", roleService.getRoles()))
                        .httpStatus(HttpStatus.OK)
                        .message("Profile Retrieved")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    // START - To reset password if user is not logged in - 3 endpoints

    @GetMapping("/reset/password/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable String email){
        userService.resetPassword(email);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .httpStatus(HttpStatus.OK)
                        .message("Email sent. Please check your email to reset the password.")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @GetMapping("/verify/password/{key}")
    public ResponseEntity<HttpResponse> verifyUrl(@PathVariable String key){
        UserDto user = userService.verifyPasswordKey(key);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", user))
                        .httpStatus(HttpStatus.OK)
                        .message("Please, enter a new password.")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @PostMapping("/reset/password/{key}/{password}/{confirmPassword}")
    public ResponseEntity<HttpResponse> updatePassword(@PathVariable("key") String key,
                                                       @PathVariable("password") String password,
                                                       @PathVariable("confirmPassword") String confirmPassword){
        userService.renewPassword(key, password, confirmPassword);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .httpStatus(HttpStatus.OK)
                        .message("Password Updated.")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    // END - To reset password if user is not logged in - 3 endpoints

    @PatchMapping("/update/password")
    public ResponseEntity<HttpResponse> updatePassword(Authentication authentication, @RequestBody @Valid UpdatePasswordForm form){
        UserDto userDto = UserUtils.getAuthenticatedUser(authentication);
        userService.updatePassword(userDto.getId(), form.getCurrentPassword(), form.getNewPassword(), form.getConfirmPassword());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .httpStatus(HttpStatus.OK)
                        .message("Password Updated Successfully")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @GetMapping("/refresh/token")
    public ResponseEntity<HttpResponse> refreshToken(HttpServletRequest request){
        if(isHeaderAndTokenValid(request)){
            String token = request.getHeader(HttpHeaders.AUTHORIZATION).substring(TOKEN_PREFIX.length());
            UserDto user = userService.getUserById(tokenProvider.getSubject(token, request));
            return ResponseEntity.ok()
                    .body(HttpResponse.builder()
                            .timestamp(LocalDateTime.now().toString())
                            .httpStatus(HttpStatus.OK)
                            .data(Map.of(
                                    "user", user,
                                    "access_token", tokenProvider.createAccessToken(getUserPrincipal(user)),
                                    "refresh_token", token
                            ))
                            .message("Token Refreshed")
                            .statusCode(HttpStatus.OK.value())
                            .build());
        }
        return ResponseEntity.badRequest()
                .body(HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .httpStatus(HttpStatus.BAD_REQUEST)
                        .message("Refresh Token missing or Invalid")
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build());
    }

    @PutMapping("/update")
    public ResponseEntity<HttpResponse> updateUser(@RequestBody @Valid UpdateUserForm user){
        UserDto updatedUser = userService.updateUserDetails(user);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", user))
                        .httpStatus(HttpStatus.OK)
                        .message("User updated")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    private Boolean isHeaderAndTokenValid(HttpServletRequest request) {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        return token != null
                && token.startsWith(TOKEN_PREFIX)
                && tokenProvider.isTokenValid(
                        tokenProvider.getSubject(token.substring(TOKEN_PREFIX.length()), request),
                        token.substring(TOKEN_PREFIX.length()));
    }

    @RequestMapping("/error")
    public ResponseEntity<HttpResponse> error(HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .httpStatus(HttpStatus.NOT_FOUND)
                        .message(request.getRequestURI() + " doesn't exist")
                        .statusCode(HttpStatus.NOT_FOUND.value())
                        .build());
    }

    @PatchMapping ("/update/role/{roleName}")
    public ResponseEntity<HttpResponse> updateUserRole(@PathVariable String roleName, Authentication authentication){
        UserDto userDto = UserUtils.getAuthenticatedUser(authentication);
        userService.updateRole(userDto.getId(), roleName);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userService.getUserById(userDto.getId()), "roles", roleService.getRoles()))
                        .httpStatus(HttpStatus.OK)
                        .message("User updated")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @PatchMapping ("/update/settings")
    public ResponseEntity<HttpResponse> updateAccountSettings(@RequestBody SettingsForm form, Authentication authentication){
        UserDto userDto = UserUtils.getAuthenticatedUser(authentication);
        userService.updateAccountSettings(userDto.getId(), form.getEnabled(), form.getNonLocked());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userService.getUserById(userDto.getId()), "roles", roleService.getRoles()))
                        .httpStatus(HttpStatus.OK)
                        .message("User account settings updated successfully.")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @PatchMapping ("/update/mfa")
    public ResponseEntity<HttpResponse> toggleMfa(Authentication authentication){
        UserDto userDto = UserUtils.getAuthenticatedUser(authentication);
        userService.toggleMfa(userDto.getId());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userService.getUserById(userDto.getId()), "roles", roleService.getRoles()))
                        .httpStatus(HttpStatus.OK)
                        .message("MFA settings updated successfully.")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @PatchMapping (value = "/update/avatar")
    public ResponseEntity<HttpResponse> updateProfileImage(Authentication authentication, @RequestParam("image") MultipartFile image){
        UserDto userDto = UserUtils.getAuthenticatedUser(authentication);
        userService.updateAvatarImage(userDto.getId(), image);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userService.getUserById(userDto.getId()), "roles", roleService.getRoles()))
                        .httpStatus(HttpStatus.OK)
                        .message("Avatar image updated successfully.")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    @GetMapping (value = "/image/{filename}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] updateProfileImage(@PathVariable String filename) throws Exception {
        return Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/Downloads/images/" + filename));
    }

    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/user/get/{user_id}").toUriString());
    }

    private ResponseEntity<HttpResponse> sendResponse(UserDto userDto) {
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of(
                                "user", userDto,
                                "access_token", tokenProvider.createAccessToken(getUserPrincipal(userDto)),
                                "refresh_token", tokenProvider.createRefreshToken(getUserPrincipal(userDto))
                        ))
                        .httpStatus(HttpStatus.OK)
                        .message("Login Success.")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    private ResponseEntity<HttpResponse> sendVerificationCode(UserDto user) {
        userService.sendVerificationCode(user);
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", user))
                        .httpStatus(HttpStatus.OK)
                        .message("Verification Code sent.")
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }

    private UserPrincipal getUserPrincipal(UserDto userDto) {
        return new UserPrincipal(UserDtoMapper.toUser(userDto),
                roleService.getRoleByUserId(userDto.getId()));
    }

    public Authentication authenticate(String email, String password){
        try {
            return authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        }catch (Exception e){
            //ExceptionUtils.processError(httpRequest, httpResponse, e);
            throw new APIException(e.getMessage());
        }
    }
}
