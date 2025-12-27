package com.spotifywrapped.spotify_wrapped_clone.service.spotify_services;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class SpotifyAuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.spotify.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.registration.spotify.scope}")
    private String scope;

    @Value("${spring.security.oauth2.client.provider.spotify.authorization-uri}")
    private String authorizationUri;

    @Value("${spring.security.oauth2.client.provider.spotify.token-uri}")
    private String tokenUri;

    @Value("${app.frontend.spotify-success-redirect:/page.html?connected=spotify}")
    private String successRedirect;


    /** ---------------------------------------------
     *   PUBLIC METHODS
     *  --------------------------------------------- */

    public String buildAuthorizationUrl(String state) {
        String encodedRedirect = URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        String encodedScope = URLEncoder.encode(scope, StandardCharsets.UTF_8);

        return authorizationUri +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + encodedRedirect +
                "&scope=" + encodedScope +
                "&state=" + state;
    }

    public SpotifyTokenResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        return requestToken(body);
    }

    public SpotifyTokenResponse refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        return requestToken(body);
    }

    public SpotifyTokenResponse requestClientCredentialsToken() {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        return requestToken(body);
    }

    public String getSuccessRedirect() {
        return successRedirect;
    }


    /** ---------------------------------------------
     *   PRIVATE HELPERS
     *  --------------------------------------------- */

    private SpotifyTokenResponse requestToken(MultiValueMap<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set(HttpHeaders.AUTHORIZATION, buildBasicAuthHeader());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<SpotifyTokenResponse> response =
                restTemplate.postForEntity(tokenUri, request, SpotifyTokenResponse.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new IllegalStateException("Spotify token request failed with status: " + response.getStatusCode());
        }

        return response.getBody();
    }

    private String buildBasicAuthHeader() {
        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }


    /** ---------------------------------------------
     *   TOKEN RESPONSE RECORD
     *  --------------------------------------------- */

    public record SpotifyTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("scope") String scope,
            @JsonProperty("expires_in") Integer expiresIn,
            @JsonProperty("refresh_token") String refreshToken
    ) { }
}
