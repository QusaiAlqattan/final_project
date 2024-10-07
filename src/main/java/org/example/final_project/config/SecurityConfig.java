package org.example.final_project.config;

import org.example.final_project.repository.SystemUserRepository;
import org.example.final_project.service.SystemUserDetailService;
import org.example.final_project.service.CustomOAuth2UserService;
import org.example.final_project.service.GitHubUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Autowired
//    private CustomOAuth2UserService customOAuth2UserService;
//
//    @Autowired
//    private GitHubUserService gitHubUserService;
//
//    // Define the security filter chain
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable()) // Proper way to disable CSRF in Spring Security 6.1+
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers("/**").permitAll()
//                        .requestMatchers("/register", "/login", "/css/**", "/js/**").permitAll() // Allow access to login page and static resources
//                        .requestMatchers("/user").hasAnyRole("USER", "ADMIN", "OAUTH2_USER")
////                        .requestMatchers("/user").permitAll()
//                        .requestMatchers("/admin").hasRole("ADMIN")
////                        .requestMatchers("/admin").permitAll()
////                        .anyRequest().authenticated() // Any other request requires authentication
//                )
//                .formLogin(form -> form
////                        .loginPage("/login") // Custom login page
//                        .successHandler(customSuccessHandler()) // Custom success handler for form login
//                        .permitAll() // Allow all users to access the login page
//                )
//                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/login")
//                        .successHandler(customSuccessHandler()) // Custom success handler for OAuth2 login
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .userService(customOAuth2UserService)) // Custom service for user details
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")  // Specify the logout URL
//                        .logoutSuccessUrl("/login?logout")  // Redirect to login page after logout
//                        .invalidateHttpSession(true)  // Invalidate the session
//                        .deleteCookies("JSESSIONID")  // Delete the session cookie
//                        .permitAll()  // Allow everyone to access the logout URL
//                );
//        return http.build(); // Build the security filter chain
//    }
//
//    @Bean
//    public UserDetailsService userDetailsService(SystemUserRepository systemUserRepository) {
//        return new SystemUserDetailService(systemUserRepository);  // Pass repository to the service
//    }
//
//    // Password encoder bean
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//
//    @Bean
//    public CustomAuthenticationSuccessHandler customSuccessHandler() {
//        return new CustomAuthenticationSuccessHandler(); // Register the custom success handler
//    }
//}
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private GitHubUserService gitHubUserService;

    // Define the security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/**").permitAll()
                        .requestMatchers("/register", "/login", "custom/**").permitAll()
                        .requestMatchers("/user").hasAnyRole("USER", "ADMIN", "OAUTH2_USER")
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(customSuccessHandler())
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(customSuccessHandler())
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(SystemUserRepository systemUserRepository) {
        return new SystemUserDetailService(systemUserRepository);
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService(null));
        return authenticationManagerBuilder.build();
    }

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return NoOpPasswordEncoder.getInstance(); // Use a proper password encoder
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CustomAuthenticationSuccessHandler customSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }
}
