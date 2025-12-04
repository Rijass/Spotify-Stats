package com.spotifywrapped.spotify_wrapped_clone.api;

import com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto.AuthResponseDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto.LoginDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto.UserDtoIn;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.userdto.UserDtoOut;
import com.spotifywrapped.spotify_wrapped_clone.service.user_services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import java.time.Duration;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDtoOut> createUser(@RequestBody UserDtoIn userDtoIn) {
        UserDtoOut user = userService.createUser(userDtoIn);
        ResponseCookie sessionCookie = buildSessionCookie(user.sessionToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                .body(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDtoOut> updateUser(@PathVariable Long id, @RequestBody UserDtoIn userDtoIn) {
        UserDtoOut updatedUser = userService.updateUser(id, userDtoIn);

        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto) {
        AuthResponseDto authResponse = userService.login(loginDto);
        if (authResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResponseCookie sessionCookie = buildSessionCookie(authResponse.sessionToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, sessionCookie.toString())
                .body(authResponse);
    }

    private ResponseCookie buildSessionCookie(String sessionToken) {
        return ResponseCookie.from("sessionToken", sessionToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .maxAge(Duration.ofDays(30))
                .path("/")
                .build();
    }

    @GetMapping("/session")
    public ResponseEntity<Void> validateSession(
            @CookieValue(value = "sessionToken", required = false) String sessionToken) {
        boolean valid = userService.isSessionValid(sessionToken);

        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = "sessionToken", required = false) String sessionToken) {

        if (sessionToken != null && !sessionToken.isBlank()) {
            userService.logout(sessionToken);
        }

        ResponseCookie deleteCookie = ResponseCookie.from("sessionToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .maxAge(0)
                .path("/")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }
}
