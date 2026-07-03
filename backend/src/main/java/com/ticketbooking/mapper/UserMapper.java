package com.ticketbooking.mapper;

import com.ticketbooking.dto.response.AuthResponse;
import com.ticketbooking.entity.User;

public final class UserMapper {

    private UserMapper() {
        // Utility class — prevent instantiation
    }

    public static AuthResponse toAuthResponse(User user, String token) {
        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().getName().name()
        );
    }
}
