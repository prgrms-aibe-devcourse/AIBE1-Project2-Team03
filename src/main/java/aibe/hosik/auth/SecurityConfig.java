package aibe.hosik.auth;

import aibe.hosik.auth.service.CustomUserDetailsService;
import aibe.hosik.user.SocialType;
import aibe.hosik.auth.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Log
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${front-end.redirect}")
    private String frontEndRedirect;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(allowedOrigins);
                    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
                    configuration.setAllowCredentials(true);
                    configuration.setMaxAge(3600L);
                    return configuration;
                }))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/signup", "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/auth/login").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/api/data/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userlnfo -> userlnfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oauth2SuccessHandler())
                )
                .authenticationProvider(daoAuthProvider())
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider() {
        DaoAuthenticationProvider prov = new DaoAuthenticationProvider();
        prov.setUserDetailsService(userDetailsService);
        prov.setPasswordEncoder(passwordEncoder());
        return prov;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(daoAuthProvider()));
    }

    @Bean
    public JwtAuthenticationFilter jwtFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
    }

    @Bean
    public AuthenticationSuccessHandler authSuccessHandler() {
        return (request, response, authentication) -> {
            String token = jwtTokenProvider.generateToken(authentication);
            response.addHeader("Authorization", "Bearer " + token);
        };
    }

    @Bean
    public AuthenticationSuccessHandler oauth2SuccessHandler() {
        return (request, response, authentication) -> {
            OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
            String regId = oauth.getAuthorizedClientRegistrationId();
            OAuth2User user = oauth.getPrincipal();

            SocialType socialType;
            try {
                socialType = SocialType.valueOf(regId.toUpperCase());
            }catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("지원하지 않음 "  + regId);
            }
            String username;
            switch (socialType) {
            case GITHUB ->
                    username = "github_" + user.getAttribute("login");
            case KAKAO  ->
                    username = "kakao_"  + user.getAttribute("id").toString();
            case GOOGLE ->
                    username = "google_" + user.getAttribute("sub").toString();
            default     -> throw new IllegalArgumentException("처리되지 않은 소셜 타입: " + socialType);
            }

            String token = jwtTokenProvider.generateToken(
                    new UsernamePasswordAuthenticationToken(username, "")
            );

            String redirectUrl = UriComponentsBuilder
                    .fromUriString(frontEndRedirect)
                    .queryParam("token", token)
                    .build().toUriString();
            response.sendRedirect(redirectUrl);
        };
    }
}
