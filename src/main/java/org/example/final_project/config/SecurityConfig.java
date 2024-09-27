package org.example.final_project.config;

import org.example.final_project.repository.SystemUserRepository;
import org.example.final_project.service.SystemUserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    UserDetailsService userDetailsService;

    // Define the config filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/register", "/login", "/css/**", "/js/**").permitAll() // Allow access to login page and static resources
                        .requestMatchers("/user").hasAnyRole("USER","ADMIN")
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated() // Any other request requires authentication
                )
                .formLogin(form -> form
                        .loginPage("/login") // Custom login page
                        .successHandler(customSuccessHandler())
                        .permitAll() // Allow all users to access the login page
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")  // Specify the logout URL
                        .logoutSuccessUrl("/login?logout")  // Redirect to login page after logout
                        .invalidateHttpSession(true)  // Invalidate the session
                        .deleteCookies("JSESSIONID")  // Delete the session cookie
                        .permitAll()  // Allow everyone to access the logout URL
                );
        return http.build(); // Build the config filter chain
    }

    @Bean
    public UserDetailsService userDetailsService(SystemUserRepository systemUserRepository) {
        return new SystemUserDetailService(systemUserRepository);  // Pass repository to the service
    }

    // Password encoder bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public CustomAuthenticationSuccessHandler customSuccessHandler() {
        return new CustomAuthenticationSuccessHandler(); // Registering the custom success handler
    }
}
