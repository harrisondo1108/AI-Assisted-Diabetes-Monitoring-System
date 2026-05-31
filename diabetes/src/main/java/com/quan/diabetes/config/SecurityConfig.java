package com.quan.diabetes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF nếu bạn làm API hoặc tùy theo nhu cầu
                .authorizeHttpRequests(auth -> auth
                        // 1. Cho phép TẤT CẢ mọi người truy cập vào các file giao diện (CSS, JS, Images)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // 2. Cho phép vào các trang public như Đăng ký, Đăng nhập mà không cần tài khoản
                        .requestMatchers("/login", "/register", "/api/auth/**").permitAll()

                        // 3. Các đường dẫn admin thì bắt buộc phải có quyền ADMIN
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 4. Tất cả các request còn lại đều phải đăng nhập
                        .anyRequest().authenticated()
                )
                // KHÔNG dùng formLogin nữa
                .formLogin(form -> form.disable())   // hoặc .formLogin().disable()
                // Tùy chọn: tắt luôn httpBasic
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}