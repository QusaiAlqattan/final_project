package org.example.final_project.service;

import org.example.final_project.config.CustomOAuth2User;
import org.example.final_project.model.SystemUser;
import org.example.final_project.repository.SystemUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService  {

    private final GitHubUserService gitHubUserService;
    private final SystemUserRepository systemUserRepository;

    @Autowired
    public CustomOAuth2UserService(GitHubUserService gitHubUserService, SystemUserRepository systemUserRepository) {
        this.gitHubUserService = gitHubUserService;
        this.systemUserRepository = systemUserRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        // Load user from OAuth2 provider (GitHub in your case)
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Add "ROLE_USER" authority
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

        // add a user to the database
        gitHubUserService.processOAuthPostLogin(oAuth2User);

        return new CustomOAuth2User(authorities, oAuth2User.getAttributes(), "login");
    }
}
