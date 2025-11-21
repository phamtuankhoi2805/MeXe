package com.example.asmproject.config;

import com.example.asmproject.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    // OAuth2LoginSuccessHandler - Đã có OAuth2 credentials
    @Autowired
    @Lazy // Trì hoãn injection để tránh circular dependency
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - không cần đăng nhập
                .requestMatchers("/", "/san-pham/**", "/phu-kien", "/dich-vu-pin", 
                               "/dich-vu-hau-mai", "/ve-chung-toi", "/tin-tuc",
                               "/dang-ky/**", "/login", "/oauth2/**", "/verify-email",
                               "/reset-password", "/khong-co-quyen", "/api/public/**", 
                               "/css/**", "/js/**", "/image/**", "/static/**").permitAll()
                
                // API Auth - public cho đăng ký, quên mật khẩu
                .requestMatchers("/api/auth/register", "/api/auth/google", 
                               "/api/auth/forgot-password", "/api/auth/reset-password",
                               "/api/auth/verify-email").permitAll()
                
                // API Auth - cần đăng nhập cho đổi mật khẩu
                .requestMatchers("/api/auth/change-password").authenticated()
                
                // API Products - public cho GET, ADMIN cho POST/PUT/DELETE
                .requestMatchers("/api/products", "/api/products/**").permitAll()
                
                // API Vouchers - public cho xem danh sách và validate
                .requestMatchers("/api/vouchers/available", "/api/vouchers/code/**").permitAll()
                
                // API Reviews - public cho xem, authenticated cho tạo
                .requestMatchers("/api/reviews/product/**").permitAll()
                .requestMatchers("/api/reviews").authenticated()
                
                // API Cart - cần đăng nhập
                .requestMatchers("/api/cart/**").authenticated()
                
                // API Addresses - cần đăng nhập
                .requestMatchers("/api/addresses/**").authenticated()
                
                // API Orders - cần đăng nhập
                .requestMatchers("/api/orders/**").authenticated()
                
                // Admin endpoints - cần ROLE_ADMIN
                .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                
                // Các request khác - cần đăng nhập
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            // OAuth2 Login - Đã có OAuth2 credentials
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(oAuth2LoginSuccessHandler) // Dùng OAuth2LoginSuccessHandler để tự động đăng ký user
                .failureUrl("/login?error=oauth_failed")
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler(accessDeniedHandler()) // Xử lý lỗi 403 - không có quyền
            );
        
        return http.build();
    }
    
    /**
     * Handler cho form login thành công (email/password)
     * OAuth2 login dùng OAuth2LoginSuccessHandler riêng
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        SimpleUrlAuthenticationSuccessHandler handler = new SimpleUrlAuthenticationSuccessHandler();
        handler.setDefaultTargetUrl("/");
        handler.setUseReferer(false);
        return handler;
    }
    
    /**
     * Handler xử lý lỗi 403 - Không có quyền truy cập
     * Redirect đến trang thông báo không có quyền
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect("/khong-co-quyen");
        };
    }
}

