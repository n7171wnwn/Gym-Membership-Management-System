package com.gym.system.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * 使用 mvcMatchers（与 DispatcherServlet 映射一致）。在配置了 context-path=/api 时，
     * antMatchers 有时无法匹配 /gym/auth/login，会导致请求落到 /gym/** 的“仅管理员/前台”规则上，登录 POST 返回 403。
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .mvcMatchers("/gym/auth/**", "/gym/health").permitAll()
            .mvcMatchers(HttpMethod.GET, "/gym/announcements").permitAll()
            .mvcMatchers(HttpMethod.GET, "/gym/courses").permitAll()

            .mvcMatchers(HttpMethod.DELETE, "/gym/manage/**").hasRole("ADMIN")

            .mvcMatchers("/gym/manage/**").hasAnyRole("ADMIN", "RECEPTION", "COACH")

            .mvcMatchers(HttpMethod.GET, "/gym/dashboard", "/gym/stats/**")
            .hasAnyRole("ADMIN", "RECEPTION", "COACH", "MEMBER")

            .mvcMatchers(HttpMethod.POST, "/gym/bookings").hasAnyRole("ADMIN", "RECEPTION", "COACH", "MEMBER")

            .mvcMatchers("/gym/member/**").hasRole("MEMBER")

            .mvcMatchers("/gym/**").hasAnyRole("ADMIN", "RECEPTION")

            .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(Collections.singletonList("*"));
        c.setAllowedMethods(Collections.singletonList("*"));
        c.setAllowedHeaders(Collections.singletonList("*"));
        c.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }

    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
