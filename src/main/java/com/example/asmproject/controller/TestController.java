package com.example.asmproject.controller;

import com.example.asmproject.model.Address;
import com.example.asmproject.model.Cart;
import com.example.asmproject.model.Product;
import com.example.asmproject.model.User;
import com.example.asmproject.repository.ProductRepository;
import com.example.asmproject.repository.UserRepository;
import com.example.asmproject.service.AddressService;
import com.example.asmproject.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/test/seed")
    @ResponseBody
    public String seedData() {
        try {
            // 1. Create or get User
            User user = userRepository.findByEmail("khoik7n@gmail.com").orElse(null);
            if (user == null) {
                user = new User();
                user.setEmail("khoik7n@gmail.com");
                user.setPassword(passwordEncoder.encode("123456"));
                user.setFullName("Test User");
                user.setPhone("0901234567");
                user.setRole(User.Role.ADMIN);
                user.setEnabled(true);
                user.setEmailVerified(true);
                user = userRepository.save(user);
            } else {
                // Update existing user to ADMIN
                user.setRole(User.Role.ADMIN);
                userRepository.save(user);
            }

            // 1b. Create Admin User
            User admin = userRepository.findByEmail("admin@vinfast.com").orElse(null);
            if (admin == null) {
                admin = new User();
                admin.setEmail("admin@vinfast.com");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setFullName("Admin User");
                admin.setPhone("0909999999");
                admin.setRole(User.Role.ADMIN);
                admin.setEnabled(true);
                admin.setEmailVerified(true);
                userRepository.save(admin);
            }

            // 2. Add Address
            if (addressService.getUserAddresses(user.getId()).isEmpty()) {
                addressService.addAddress(user.getId(), "Test User", "0901234567", "Hà Nội", "Thanh Xuân",
                        "Thanh Xuân Trung", "123 Nguyễn Trãi");
                addressService.addAddress(user.getId(), "Test User Office", "0987654321", "Hồ Chí Minh", "Quận 1",
                        "Bến Nghé", "456 Lê Duẩn");
            }

            // 3. Add Product to Cart
            Product product = productRepository.findAll().stream().findFirst().orElse(null);
            if (product == null) {
                product = new Product();
                product.setName("VinFast Evo200");
                product.setPrice(java.math.BigDecimal.valueOf(22000000.0));
                product.setQuantity(100);
                product.setDescription("Xe máy điện quốc dân");
                product.setImage("evo200.jpg");
                product.setStatus(Product.ProductStatus.ACTIVE);
                product = productRepository.save(product);
            }

            cartService.addToCart(user.getId(), product.getId(), null, 1);

            return "Data seeded successfully! User: khoik7n@gmail.com / 123456";
        } catch (Exception e) {
            return "Error seeding data: " + e.getMessage();
        }
    }
}
