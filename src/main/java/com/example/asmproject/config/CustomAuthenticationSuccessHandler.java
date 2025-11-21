package com.example.asmproject.config;

import com.example.asmproject.model.User;
import com.example.asmproject.service.AddressService;
import com.example.asmproject.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

/**

 */
@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        // Lấy email từ authentication
        String email = null;
        if (authentication.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            email = (String) authentication.getPrincipal();
        }

        // Kiểm tra nếu user mới (chưa có phone hoặc chưa có address)
        if (email != null) {
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                boolean isNewUser = (user.getPhone() == null || user.getPhone().isEmpty())
                        || addressService.getUserAddresses(user.getId()).isEmpty();

                if (isNewUser) {
                    // Redirect đến trang tài khoản để nhập thông tin
                    getRedirectStrategy().sendRedirect(request, response, "/tai-khoan");
                    return;
                }
            }
        }

        // Nếu không phải user mới thì redirect về trang chủ
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
