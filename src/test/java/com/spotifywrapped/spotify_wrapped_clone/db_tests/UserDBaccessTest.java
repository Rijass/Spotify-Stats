package com.spotifywrapped.spotify_wrapped_clone.db_tests;

import com.spotifywrapped.spotify_wrapped_clone.dbaccess.UserDBaccess;
import com.spotifywrapped.spotify_wrapped_clone.dbaccess.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(UserDBaccess.class)
class UserDBaccessTest {

    @Autowired
    private UserDBaccess userDBaccess;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void createUserPersistsAndCanBeFound() {
        User user = buildUser("alice", "alice@example.com", "secret");

        User created = userDBaccess.createUser(user);
        entityManager.flush();
        entityManager.clear();

        User found = userDBaccess.findUserById(created.getId());

        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("alice");
        assertThat(found.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void updateUserReturnsNullWhenUserDoesNotExist() {
        User updates = buildUser("bob", "bob@example.com", "pw");

        User updated = userDBaccess.updateUser(999L, updates);

        assertThat(updated).isNull();
    }

    @Test
    void updateUserAppliesPartialChanges() {
        User original = userDBaccess.createUser(buildUser("carol", "carol@example.com", "pw1"));
        entityManager.flush();
        entityManager.clear();

        User updates = new User();
        updates.setEmail("new@example.com");

        User updated = userDBaccess.updateUser(original.getId(), updates);
        entityManager.flush();
        entityManager.clear();

        User reloaded = userDBaccess.findUserById(original.getId());

        assertThat(updated).isNotNull();
        assertThat(reloaded.getEmail()).isEqualTo("new@example.com");
        assertThat(reloaded.getUsername()).isEqualTo("carol");
    }

    @Test
    void deleteUserReturnsFalseWhenMissing() {
        boolean deleted = userDBaccess.deleteUser(12345L);

        assertThat(deleted).isFalse();
    }

    @Test
    void findByUsernameOrEmailSupportsBothIdentifiers() {
        userDBaccess.createUser(buildUser("dave", "dave@example.com", "pw"));
        entityManager.flush();
        entityManager.clear();

        User byUsername = userDBaccess.findByUsernameOrEmail("dave");
        User byEmail = userDBaccess.findByUsernameOrEmail("dave@example.com");
        User missing = userDBaccess.findByUsernameOrEmail("unknown");

        assertThat(byUsername).isNotNull();
        assertThat(byEmail).isNotNull();
        assertThat(byUsername.getId()).isEqualTo(byEmail.getId());
        assertThat(missing).isNull();
    }

    @Test
    void deleteUserRemovesExistingUser() {
        User user = userDBaccess.createUser(buildUser("erin", "erin@example.com", "pw"));
        entityManager.flush();
        entityManager.clear();

        boolean deleted = userDBaccess.deleteUser(user.getId());
        entityManager.flush();
        entityManager.clear();

        User removed = userDBaccess.findUserById(user.getId());

        assertThat(deleted).isTrue();
        assertThat(removed).isNull();
    }

    private User buildUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}
