package io.vital.billspace.controller;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.dto.dtomapper.UserDtoMapper;
import io.vital.billspace.form.LoginForm;
import io.vital.billspace.model.HttpResponse;
import io.vital.billspace.model.User;
import io.vital.billspace.model.UserPrincipal;
import io.vital.billspace.provider.TokenProvider;
import io.vital.billspace.service.UserService;
import io.vital.billspace.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authManager;
    private final TokenProvider tokenProvider;
    private final RoleService roleService;

    @PostMapping("/login")
    public ResponseEntity<HttpResponse> login(@RequestBody @Valid LoginForm loginForm){
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginForm.getEmail(), loginForm.getPassword()));
        UserDto userDto = userService.getUserByEmail(loginForm.getEmail());
        return userDto.isUsingMFA() ? sendVerificationCode(userDto) : sendResponse(userDto);
    }

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

    @GetMapping("/verify/code/{email}/{code}")
    public ResponseEntity<HttpResponse> saveUser(@PathVariable("email") String email,
                                                 @PathVariable("code") String code){
        UserDto user = userService.verifyCode(email, code);
        return sendResponse(user);
    }

    @GetMapping("/verify/account/{token}")
    public ResponseEntity<HttpResponse> verifyUser(@PathVariable String token){
        Boolean isVerified = userService.verifyUser(token);
        return ResponseEntity.status(isVerified ? HttpStatus.OK.value() : HttpStatus.CONFLICT.value())
                .body(HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("isVerified", isVerified))
                        .httpStatus(isVerified ? HttpStatus.OK : HttpStatus.CONFLICT)
                        .message(isVerified ? "User account has been verified" : "User account has not been verified")
                        .statusCode(isVerified ? HttpStatus.OK.value() : HttpStatus.CONFLICT.value())
                        .build());
    }

    @GetMapping("/profile")
    public ResponseEntity<HttpResponse> profile(Authentication authentication){
        UserDto user = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok().body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", user))
                        .httpStatus(HttpStatus.OK)
                        .message("Profile Retrieved")
                        .statusCode(HttpStatus.OK.value())
                        .build());
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
                roleService.getRoleByUserId(userDto.getId()).getPermission());
    }

}
