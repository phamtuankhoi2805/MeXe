package com.example.asmproject.service;

import com.example.asmproject.model.User;
import com.example.asmproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndEnabledTrue(email)
            .orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại hoặc đã bị vô hiệu hóa: " + email));
        
        // Kiểm tra email đã được xác thực chưa (chỉ áp dụng cho đăng nhập bằng email/password)
        // Nếu đăng nhập bằng Google thì email đã được Google xác thực
        if (user.getProvider() == null && !user.getEmailVerified()) {
            throw new UsernameNotFoundException("Email chưa được xác thực. Vui lòng kiểm tra email và xác thực tài khoản.");
        }
        
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password(user.getPassword() != null ? user.getPassword() : "")
            .authorities(getAuthorities(user))
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!user.getEnabled())
            .build();
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
}

