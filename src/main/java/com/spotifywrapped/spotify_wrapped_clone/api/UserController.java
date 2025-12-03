package com.spotifywrapped.spotify_wrapped_clone.api;

import com.spotifywrapped.spotify_wrapped_clone.api.dto.AuthResponseDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.LoginDto;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.UserDtoIn;
import com.spotifywrapped.spotify_wrapped_clone.api.dto.UserDtoOut;
import com.spotifywrapped.spotify_wrapped_clone.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
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
        return ResponseEntity.ok(authResponse);
    }
}
