package io.vital.billspace.controller;

import io.vital.billspace.dto.UserDto;
import io.vital.billspace.dto.dtomapper.UserDtoMapper;
import io.vital.billspace.model.HttpResponse;
import io.vital.billspace.model.User;
import io.vital.billspace.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/register")
    public ResponseEntity<HttpResponse> saveUser(@RequestBody @Valid User user){
        UserDto userDto = userService.createUser(user);
        return ResponseEntity.status(201).body(
                HttpResponse.builder()
                        .timestamp(LocalDateTime.now().toString())
                        .data(Map.of("user", userDto))
                        .httpStatus(HttpStatus.CREATED)
                        .message("User has been created")
                        .statusCode(HttpStatus.CREATED.value())
                        .build());
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

    private URI getUri() {
        return URI.create(ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/user/verify/<user_id>").toUriString());
    }

}
