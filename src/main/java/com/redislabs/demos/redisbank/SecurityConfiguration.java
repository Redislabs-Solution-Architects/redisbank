// SecurityConfiguration.java
package com.redislabs.demos.redisbank;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;
import org.springframework.web.cors.*;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  /* CORS for Vite dev */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    var c = new CorsConfiguration();
    c.setAllowedOrigins(List.of("http://localhost:5173"));
    c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
    c.setAllowedHeaders(List.of("*"));
    c.setAllowCredentials(true);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", c);
    return source;
  }

  // -------- Chain #1: ONLY for WebSocket handshake --------
  @Bean
  @Order(1)
  public SecurityFilterChain wsFilterChain(HttpSecurity http) throws Exception {
    http
      .securityMatcher("/ws", "/ws/**")   // match exact /ws and subpaths
      .cors(Customizer.withDefaults())
      .csrf(AbstractHttpConfigurer::disable)
      .headers(h -> h.frameOptions(f -> f.disable()))
      .authorizeHttpRequests(a -> a.anyRequest().permitAll())
      // pas de formLogin ici
      .httpBasic(AbstractHttpConfigurer::disable);
    return http.build();
  }

  // -------- Chain #2: the rest (HTML, API, etc.) --------
  @Bean
  @Order(2)
  public SecurityFilterChain appFilterChain(HttpSecurity http) throws Exception {
    http
      .cors(Customizer.withDefaults())
      .csrf(AbstractHttpConfigurer::disable)
      .headers(h -> h.frameOptions(f -> f.disable()))
      .authorizeHttpRequests(req -> req
        .requestMatchers(
          "/", 
          "/index.html", 
          "/auth-login.html",
          "/assets/**",
          "/perform_login",
          "/api/**",
          "/error"            // ⬅️ IMPORTANT : public, sinon 302 sur handshake échoué
        ).permitAll()
        .anyRequest().authenticated()
      )
      .formLogin(form -> form
        .loginPage("/")
        .loginProcessingUrl("/perform_login")
        .successHandler((req, res, auth) -> res.setStatus(200))
        .defaultSuccessUrl("/index.html")
        .failureUrl("/auth-login.html?error=true")
        .permitAll()
      )
      .logout(LogoutConfigurer::permitAll);
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder encoder) {
    UserDetails user = User.withUsername("lars")
        .password(encoder.encode("larsje"))
        .roles("USER")
        .build();
    return new InMemoryUserDetailsManager(user);
  }

  @Bean
  public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}