package com.hendisantika.controller;

import com.hendisantika.dto.RegistrationRequest;
import com.hendisantika.model.Role;
import com.hendisantika.model.User;
import com.hendisantika.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registration", new RegistrationRequest());
        return "register";
    }

    @PostMapping("/register")
    public String createAccount(@Valid @ModelAttribute("registration") RegistrationRequest registration,
                                BindingResult bindingResult) {
        if (registration.getPassword() != null
                && !registration.getPassword().equals(registration.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Passwords do not match");
        }
        String username = registration.getUsername() == null ? "" : registration.getUsername().trim();
        String email = registration.getEmail() == null ? "" : registration.getEmail().trim();
        if (!username.isEmpty() && userRepository.existsByUsername(username)) {
            bindingResult.rejectValue("username", "username.exists", "That username is already in use");
        }
        if (!email.isEmpty() && userRepository.existsByEmail(email)) {
            bindingResult.rejectValue("email", "email.exists", "That email is already registered");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(registration.getPassword()))
                .firstName(registration.getFirstName().trim())
                .lastName(registration.getLastName().trim())
                .role(Role.ROLE_USER)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        userRepository.save(user);
        return "redirect:/login?registered=true";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
