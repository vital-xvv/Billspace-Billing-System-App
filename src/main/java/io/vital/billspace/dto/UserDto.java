package io.vital.billspace.dto;

import lombok.*;

import java.time.LocalDateTime;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String address;
    private String phone;
    private String title;
    private String bio;
    private String imageURL;
    private boolean enabled;
    private boolean isNotLocked;
    private boolean isUsingMFA;
    private LocalDateTime createdAt;
    private String roleName;
    private String permissions;
}
