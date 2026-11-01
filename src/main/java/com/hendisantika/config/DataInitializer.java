package com.hendisantika.config;

import com.hendisantika.model.Role;
import com.hendisantika.model.Category;
import com.hendisantika.model.Coupon;
import com.hendisantika.model.Product;
import com.hendisantika.repository.CategoryRepository;
import com.hendisantika.repository.ProductRepository;
import com.hendisantika.model.User;
import com.hendisantika.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * Project : spring-boot-ecommerce
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 18/11/25
 * Time: 06.30
 * To change this template use File | Settings | File Templates.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws IOException {
        log.info("Initializing default users...");

        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@shophub.com")
                    .firstName("Admin")
                    .lastName("User")
                    .role(Role.ROLE_ADMIN)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            userRepository.save(admin);
            log.info("✅ Admin user created: username=admin, password=admin123");
        }

        // Create regular user if not exists
        if (!userRepository.existsByUsername("user")) {
            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("user123"))
                    .email("user@shophub.com")
                    .firstName("John")
                    .lastName("Doe")
                    .role(Role.ROLE_USER)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();
            userRepository.save(user);
            log.info("✅ Regular user created: username=user, password=user123");
        }

        log.info("✅ Default users initialization complete!");
        seedCatalog();
    }

    private void seedCatalog() throws IOException {
        if (productRepository.count() > 0) {
            return;
        }

        Category footwear = saveCategory("Footwear");
        Category electronics = saveCategory("Electronics");
        Category accessories = saveCategory("Accessories");
        Category furniture = saveCategory("Furniture");

        saveProduct("Everyday Runner", "Red lightweight running shoes with a cushioned sole.", 45,
                89.99, "Nike", 15, "red-sneakers.jpg", footwear);
        saveProduct("Studio Wireless Headphones", "Over-ear wireless headphones for work and travel.", 32,
                129.99, "Sony", 10, "wireless-headphones.jpg", electronics);
        saveProduct("Classic Minimal Watch", "A clean analog watch with a stainless-steel case.", 20,
                149.99, "Casio", 5, "minimal-watch.jpg", accessories);
        saveProduct("Pocket Instant Camera", "Compact instant camera for capturing everyday moments.", 18,
                99.99, "Fujifilm", 20, "instant-camera.jpg", electronics);
        saveProduct("Modern Accent Chair", "Comfortable upholstered accent chair for a living space.", 7,
                249.99, "IKEA", 0, "accent-chair.jpg", furniture);
        log.info("Seeded sample catalog products");
    }

    private Category saveCategory(String name) {
        Category category = categoryRepository.findByName(name);
        if (category != null) {
            return category;
        }
        category = new Category();
        category.setName(name);
        return categoryRepository.save(category);
    }

    private void saveProduct(String name, String description, int quantity, double price, String brand,
                             int discount, String imageName, Category category) throws IOException {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setQuantity(quantity);
        product.setPrice(price);
        product.setBrand(brand);
        product.setDiscount(Coupon.builder().discount(discount).build());
        try (InputStream image = new ClassPathResource("static/images/products/" + imageName).getInputStream()) {
            product.setImage(Base64.getEncoder().encodeToString(image.readAllBytes()));
        }
        product.setCategories(Set.of(category));
        productRepository.save(product);
    }
}
