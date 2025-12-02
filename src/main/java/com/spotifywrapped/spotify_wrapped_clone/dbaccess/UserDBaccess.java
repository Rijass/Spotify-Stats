package com.spotifywrapped.spotify_wrapped_clone.dbaccess;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class UserDBaccess {

    @PersistenceContext
    private EntityManager entityManager;


}
