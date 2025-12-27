package com.spotifywrapped.spotify_wrapped_clone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpotifyWrappedCloneApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpotifyWrappedCloneApplication.class, args);
	}

}
