package org.demo.auth.config;


import lombok.RequiredArgsConstructor;
import org.demo.auth.utils.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtTokenFilter jwtTokenFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        // Enforce HTTPS
        //.requiresChannel(channel -> channel.anyRequest().requiresSecure())
        .csrf(AbstractHttpConfigurer::disable)
        .securityMatcher("/api/**")
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/api/v1/user/login", "/api/v1/user/register").permitAll() // Public endpoints
            .requestMatchers("/api/v1/demo/**").hasRole("ADMIN")  // Role-based access
            .anyRequest().authenticated())
        .addFilterBefore(this.jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling((exception)-> exception.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .build();
  }

}
