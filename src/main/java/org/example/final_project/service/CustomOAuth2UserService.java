package org.example.final_project.service;

import org.example.final_project.model.SystemUser;
import org.example.final_project.repository.SystemUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final GitHubUserService gitHubUserService;
    private final SystemUserRepository systemUserRepository;

    @Autowired
    public CustomOAuth2UserService(GitHubUserService gitHubUserService, SystemUserRepository systemUserRepository) {
        this.gitHubUserService = gitHubUserService;
        this.systemUserRepository = systemUserRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // Process the user info from GitHub
        gitHubUserService.processOAuthPostLogin(oAuth2User);
//        System.out.println("oAuth2User = " + oAuth2User.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("OAUTH2_USER")));
        return oAuth2User;
    }
}
